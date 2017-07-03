# FOOL - Functional Object Oriented Language

***Progetto del corso di Compilatori ed Interpreti AA 2016/2017***

***Corso di Laurea Magistrale in Informatica, Università di Bologna***

***Componenti del gruppo***

- Alberto Nicoletti (matricola 819697)
- Devid Farinelli (matricola 819683)
- Mirco Civolani (matricola 798717)
- Pietro Battilana (matricola 799486)

------

***Tabella dei contenuti***

[TOC]

## 1. Struttura del progetto

Il progetto del corso prevede l'implementazione di un compilatore per codice sorgente `FOOL` che generi delle istruzioni `SVM` e le esegua su un calcolatore emulato. 

Sono state realizzate **entrambe** le richieste opzionali nella consegna del progetto, ovvero garbage collection ed estensioni con gli operatori (`<`, `>`, `<=`, `>=`, `||`, `&&`, `/`, `-`,  `!`)

Il progetto è sviluppato in Java utilizzando l'IDE IntelliJ IDEA e le librerie di `ANTLR v4.7`.  

La cartella `src` contiene il codice sorgente che è suddiviso in diversi package:

- `exception`

  contiene le classi che implementano le eccezioni sintattiche, semantiche e di run-time con i relativi messaggi d'errore

- `grammar`

  contiene le grammatiche in formato `.g4` del linguaggio `FOOL` e del linguaggio `SVM`. A partire da questi file, ANTLR genera le risorse necessarie per implementare Lexer, Parser e Visitor

- `main`

  contiene le classi necessarie alla sequenzializzazione delle varie fasi di esecuzione di un programma `FOOL`: analisi lessicale e sintattica, analisi semantica, code generation ed esecuzione effettiva. Sono disponibili le modalitá:

  - `TestDebug`, che prende come input un solo programma contenuto in `input.fool`
  - `TestComplete` , che esegue in sequenza tutti i programmi contenuti in `test.yml`

- `node`

  contiene una classe per ogni nodo dell'AST creato dal lexer. Ciascuno dei nodi implementa i metodi necessari per il controllo semantico, il type checking (visita bottom-up) e la code generation (visita top-down)

- `symbol_table`

  contiene la tabella dei simboli, implementata con una lista di hashtable, utilizzata durante il controllo semantico in caso di dichiarazione o di riferimento ad una variabile

- `type`

  contiene le classi che implementano i tipi primitivi del linguaggio `FOOL`. Ad ogni nodo dell'albero corrispone un tipo che viene utilizzato per il controllo semantico, il type checking e come entry della tabella dei simboli per le variabili e le funzioni. 

- `util`

  contiene metodi di utilitá, principalmente utility usate in fase di code generation per la creazione di label e la generazione delle dispatch tables

- `vm`

  contiene le classi che emulano l'archittetura e l'instruction set di un calcolatore dotato di una memoria gestita in parte come stack e in parte come heap

### 1.1 Installazione ed esecuzione

Spiegare le modalità per importare e eseguire il progetto ... TODO

## 2. Analisi lessicale e sintattica

In questa sezione si descriveranno le grammatiche che definiscono i linguaggi `FOOL` e  `SVM`, con particolare attenzione alle motivazioni che hanno portato alle modifiche delle grammatiche originali.

### 2.1 Grammatica FOOL

#### 2.1.1 Met

È stato aggiunto il non terminale `met`  per gestire separatamente le definizioni di metodi dalle definizioni di funzioni a livello semantico e di code generation. A riga 10 è possibile vedere come `met` sia solo un wrapper per il non terminale `fun` utilizzato all'interno di `classdec`:

```ANTLR
prog
    : exp SEMIC                 		    #singleExp
    | let exp SEMIC                 	    #letInExp
    | (classdec)+ SEMIC (let)? exp SEMIC	#classExp
    ;

classdec
    : CLASS ID ( IMPLEMENTS ID )? (LPAR (vardec ( COMMA vardec)*)? RPAR)?  (CLPAR ((met SEMIC)+)? CRPAR)?;
    
met : fun;
    
fun : type ID LPAR ( vardec ( COMMA vardec)* )? RPAR (let)? exp;
```

#### 2.1.2 funExp e methodExp

Per permettere di utilizzare il valore di ritorno delle chiamate di funzioni e di metodi come fossero `exp`, sono state create due produzioni sotto `value`:

- `#funExp` che rappresenta la chiamata di una funzione
- `#methodExp` che rappresenta la chiamata di un metodo

Queste due etichette permettono di gestire i nodi separatamente durante l'analisi lessicale implementando logiche diverse per i metodi `visitFunExp` e `visitMethodExp`:

```ANTLR
value
    :  ...
    | funcall       							#funExp
    | (ID | THIS) DOT funcall                   #methodExp 
    |  ... ;

funcall : ID ( LPAR (exp (COMMA exp)* )? RPAR ) ;
```

### 2.1.3 Operatori aggiuntivi

Gli operatori aggiuntivi sono stati realizzati semplicemente aggiungendo delle opzioni per la riduzione del non terminale `operator`:

```ANTLR
exp :  ('-')? left=term (operator=(PLUS | MINUS) right=exp)? ;

term : left=factor (operator=(TIMES | DIV) right=term)? ;

factor : 
left=value (operator=(AND | OR | GEQ | EQ | LEQ | GREATER | LESS) right=value)? ;
```



### 2.2 Grammatica SVM

#### 2.2.1 COPY

Per semplificare  il riutilizzo dei valori sullo stack è stata aggiunta l'istruzione `COPY` che duplica il valore in cima alla pila. L'istruzione è implementata come segue:

```ANTLR
| COPY                        {   code.add(COPY);     }
```

```java
case SVMParser.COPY:
	push(memory[sp - MEMORY_START_ADDRESS]);
	break;
```

#### 2.2.1 l = LABEL

// TODO

#### 2.2.2 LC

Per implementare le chiamate di funzioni è stata aggiunta l'istruzione `LC` che permette di ottenere l'indirizzo del codice di una funzione partendo dalla sua label:

```ANTLR
| LC                          {   code.add(LC);       }
```

L'istruzione `LC` si aspetta in cima allo stack la label di una funzione, rimuove il valore dalla pila e lo usa come indice nell'array `code`, pushando il valore ottenuto in cima allo stack:

```java
case SVMParser.LC:
	int codeAddress = pop();
    push(code[codeAddress]);
	break;
```

#### 2.2.4 NEW

//TODO

#### 2.2.5 HOFF

L'istruzione `HOFF` (heap offset) converte l'offset del campo di un oggetto nell'offset reale tra l'indirizzo dell'oggetto nello heap ed l'indirizzo del campo. Viene utilizzato accedendo ad un campo per gestire il caso in cui gli oggetti siano memorizzati in celle non contigue di memoria.

L'istruzione `HOFF` è implementata come segue:

```ANTLR
| HOFF                        {   code.add(HOFF);     }
```

```java
case SVMParser.HOFF:
	int objAddress = pop(); // indirizzo di this
    int objOffset = pop(); // offset logico rispetto all'oggetto
    HeapMemoryCell list = heapMemoryInUse
    						.stream()
                            .filter(cell -> cell.getIndex() == objAddress)
                            .reduce(new HeapMemoryCell(0, null), (prev, curr) -> curr);
    for (int i = 0; i < objOffset; i++) {
    	list = list.next;
    }
    int fieldAddress = list.getIndex();
    int realOffset = fieldAddress - objAddress;
    push(realOffset);
    push(objAddress);
    break;
```

La struttura ed il funzionamento dello heap dove vengono memorizzate le istanze di oggetti verranno discusse successivamente,  in questo esempio viene utilizzato il metodo `stream()` di Java 8 per lavorare su collezioni senza usare cicli.



// TODO sistemare questo sotto, non fa parte della grammatica

Si è resa la dimensione dell'array `code`, contenente il bytecode, variabile a seconda del codice SVM prodotto dal compilatore FOOL. Ciò è stato fatto cambiando l'array `int[] code` nella sezione annotata come *@parser:members* in un private `ArrayList<Integer> code` di dimensioni inizialmente nulle.  Nelle regole per l'*assembly* per aggiungere un istruzione si chiama `code.add(instruction_int_code)`. In tal modo il codice sarà lungo esattamente quanto necessario senza sprechi di memoria. Si è modificato leggermente di conseguenza anche il *backpatching* per accedere ad ArrayList. 



### 2.3 Nodi

Ad ogni nodo dell'albero sintattico corrisponde una classe che implementa l'interfaccia `INode` che rispetto alla versione originale è stata modificata come segue:

- Il metodo `toPrint()` è stato sostituito dal metodo `toString()` nativo di Java
- Per poter stampare l'AST è stato aggiunto il metodo `ArrayList<INode> getChilds()` che restituisce i figli del nodo attuale

#### 2.3.2 Nodi operatore

La grammatica inziale permetteva definiva solamente l'operatore `==`, è stata quindi estesa per supportare anche:

- gli operatori sottrazione e divisione `-` e `/`


- gli operatori per il confronto fra interi  `<=`, `>=`, `<` e `>` 
- gli operatori booleani  `&&`, `||` e `!` 

Ogni nodo operatore (escluso il NOT) presenta ha due attributi:

- `INode left`,  l'operando sinistro
- `INode right`,  l'operando destro

mentre il nodo operatore NOT ha solamente un `INode` figlio che è il `BoolNode` su cui viene applicato



## 3. Analisi semantica

### 3.1 Symbol Table

Per discutere la nostra implementazione della fase di analisi semantica è fondamentale partire dalla struttura della symbol table.  La tabella dei simboli fa parte dell'ambiente (istanza della classe `Environment`) che viene passato ad ogni nodo dell'AST per eseguire la sua analisi semantica. Come detto in precedenza, la tabella dei simboli è stata implementata con una lista di hashtable:

`private ArrayList<HashMap<String, SymbolTableEntry>> symbolTable = new ArrayList<>();`

che come si può osservare è stata resa pubblica. Sono stati aggiunti infatti i metodi necessari per accedere alla tabella con le varie modalità (aggiungere una hashtable, aggiungere, cercare o modificare una entry).  Se si incontra un `prog` che dichiara definizioni di classi e variabili (seconda e terza produzioni) allora viene aggiunta una hashmap alla `symbolTable` su cui si opererà con i metodi:

- `addEntry(String id, Type type, int offset)`

  inserisce nella più recente hashmap un oggetto `SymbolTableEntry` con nome e tipo dati come parametro alla funzione.

- `getLatestEntryOf(String id)`

  che sfrutta oggetti di tipo `Iterator` per scorrere la lista di tabella hash ed in ognuna di queste controlla se è contenuta un'entrata con un `id` uguale a quello passato come parametro. In caso positivo l'entrata viene ritornata, in caso negativo viene sollevata una `UndeclaredVarException`.

- `setEntryType(String id, Type newtype, int offset)`

  questo metodo è stato introdotto per permettere di fare riferimento ad una classe all'interno di un altra definita precedentemente nel codice.  Prima di procedere al controllo semantico di ogni classe, viene fatta un inserzione nella tabella dei simboli di una entry 'incompleta' di tutte le classi e successivamente con questo metodo la entry 'incompleta' viene sostituita a quella con il `type` correttamente costruito.




### 3.2 Dichiarazione di classi

Passiamo ora a discutere il funzionamento del controllo semantico nel caso di programmi FOOL contenenti dichiarazioni di classi. Innanzitutto il metodo `visitClassExp` della classe FoolVisitorImpl.java avendo a disposizione tutto il codice parsato instanzia oggetti delle varie classi nodo (`ParameterNode` per i campi e `MethodNode` per i metodi che insieme concorrono a creare `ClassNode`, `LetNode` per le dichiarazioni di variabili e funzioni e `InNode` per l'espressione conclusiva). Tutti questi oggetti vengono passati al costruttore dell'oggetto `ProgClassDecNode` che costituisce il nodo radice dell'AST. È proprio dalla radice, andando verso le foglie che parte il controllo semantico, lanciato dal metodo `semanticAnalysis` della classe FoolRunner.java.



Il controllo semantico in `ProgClassDecNode`, per prima cosa, si occupa di eseguire un inserzione preliminare delle classi nella symbol table (come detto in precedenza). Per ogni elemento della lista `classDeclarations`,  istanza di `ClassNode`, viene poi chiamato il controllo semantico su di essa. 



#### 3.2.1 Class Node

La classe `ClassNode` dispone di:

| Campo          | Tipo                       | Descrizione                              |
| -------------- | -------------------------- | ---------------------------------------- |
| `classID`      | `String`                   | Id della classe                          |
| `superClassID` | `String`                   | Id della eventuale superclasse           |
| `attrDecList`  | `ArrayList<ParameterNode>` | Lista dei nodi campi (passata dal costruttore) |
| `metDecList`   | `ArrayList<MethodNode>`    | Lista dei nodi metodi (passata dal costruttore) |
| `fields`       | `HashMap<String, Type>`    | Mappa nome-tipo dei campi                |
| `methods`      | `HashMap<String, FunType>` | Mappa nome-tipo dei campi                |
| `type`         | `ClassType`                | Tipo della classe                        |



#### 3.2.2 Field

La classe `Field` dispone di:

| Campo  | Tipo     | Descrizione    |
| ------ | -------- | -------------- |
| `id`   | `String` | Id della campo |
| `type` | `Type`   | Tipo del campo |



#### 3.2.2 Method

La classe `Method` dispone di:

| Campo  | Tipo      | Descrizione             |
| ------ | --------- | ----------------------- |
| `id`   | `String`  | Id della campo          |
| `type` | `FunType` | Tipo (firma) del metodo |



#### 3.2.3 Validazione dichiarazione di classe

La **validazione semantica di una classe** ha i seguenti passi:

1. Per ogni campo `a` di  `attrDecList` creare un oggetto `Field` passando id e tipo ottenuti da `a`
2. Aggiungere questo oggetto ad un `ArrayList<Field> fieldsList` e alla mappa `fields`  
3. Per ogni metodo `m` di  `metDecList`:
   - ciclare sui parametri `p` del metodo `m` 
   - aggiungere il tipo `t` di `p` all'`ArrayList<Type> paramsType`
     - se `t` è un  ID di una classe `c`, cercare `c` nella symbol table ed inserire il relativo `ClassType` in `paramsType`. Se non si è trovata `c` allora si sollevare un **eccezione** di classe non dichiarata.
   - infine creare un oggetto `fun` di tipo `FunType` passando il tipo di ritorno di `m` e i `paramsType`
4. Aggiungere l'oggetto `fun` ad un `ArrayList<Method> methodList` e alla mappa `methods`  
5. Cercare nella symbol table il tipo `superType` della super classe: `getLatestEntryOf(superClassID)`
6. Assegnare a `this.type` un `ClassType` creato passando: 
   - `classID`
   - `superType`
   - `fieldsList`
   - `methodList`
7. Aggiornare la entry con `classID` come chiave nella symbol table con il nuovo `this.type`
8. Fare un push di una nuova symbol hashtable (incrementando il nesting level)
9. Per ogni attributo `var` in `attrDecList`:
   - se `var` è di tipo sotto classe rispetto alla classe in dichiarazione allora sollevare un eccezione. <u>**Non** è consentito utilizzare sottoclassi come campi della superclasse</u>, altrimenti sarebbe impossibile istanziare la sottoclasse.
   - altrimenti, creare ed inserire una entry all'interno della symbol table per `var` con il suo tipo
10. Fare un altro push di una nuova symbol hashtable (incrementando il nesting level)
11. Per ogni metodo `fun` in `metDecList`:
    - controllare che abbia un id ed un tipo di ritorno (?)  // TODO
    - se il tipo di ritorno è un `InstanceType`  aggiornare il suo campo `classType` con il tipo di classe recuperato dalla symbol table una classe. Se tale classe non è contenuta nella symbol table sollevare un eccezione.
    - altrimenti, creare ed inserire una entry all'interno della symbol table per `fun`
    - fare push di una nuova symbol hashtable (incrementando il nesting level)
    - aggiungere una entry con chiave `this` avente `InstanceType` della classe che contiene `fun`
    - aggiungere una entry per ogni parametro dichiarato da `fun`
    - chiamare il metodo `checkSemantics` sul espressione che costituisce il body di `fun`
    - infine fare un pop dell'ultima symbol hashtable (decrementando il nesting level)
12. Fare due pop di symbol table poichè si sono conclusi gli *scope* di campi e metodi
13. Se la classe `C` estende un'altra classe `Super`:
    - recuperare dalla symbol table il tipo `supertype` di `Super` (se non esiste o non è una classe sollevare un eccezione)
    - scorrere `supertype.fields` con un indice `i = 0`
      - se `fields.size()` >= `supertype.fields.size()` e
      - se `supertype.fields[i].id` è uguale a `fields[i].id` e `fields[i]` è sottotipo di `supertype.fields[i]`
      - allora `C` sta estendendo correttamente `Super` per quanto riguarda i campi, altrimenti vengono sollevati i corrispondenti `SemanticError`
    - per ogni metodo `m` di `C`:
      - se esiste un omonimo (i.e. `Super.m()`) nella superclasse controllare che `C.m()` lo sottotipi correttamente (si rimanda alla sezione 4 per i dettagli)
      - altrimenti vengono sollevati i corrispondenti `SemanticError`

## 4. Type checking

Il type checking del programma FOOL in input dal compilatore viene eseguito subito dopo l'analisi semantica e ogni nodo padre chiama quello del figlio. Il controllo del tipaggio corretto viene però logicamente svolto in ordine bottom-up rispetto ai nodi dell'AST. Ogni `INode` presenta un metodo `type()` che restituisce il tipo di quel nodo. Prima questo tipo, avviene un controllo sui tipi dei figli, e se presenti, e sui propri parametri e/o argomenti. 



Il tipo dell'intero AST, ritornato dal metodo `type()` della radice, è il tipo della espressione `exp` finale del programma FOOL.

### 4.1 Type system

Durante il controllo dei tipi si va a controllare che, per un determinato nodo, l'operazione, la funzione o la classe, o più in generale tutti i componenti di quel nodo rispettino le regole di tipaggio. Ogni struttura quindi ha le proprie regole di typing che sono diverse dalle altre, come ad esempio le regole di subtyping. Abbiamo scelto quindi di creare un'interfaccia `Type` che presenta i seguenti metodi su cui basare le regole di tipaggio: 

- `String getID()` - restituisce un valore dell'enumerazione `TypeID` 

- `boolean isSubtypeOf(Type t)` - restituisce vero se la classe tipo da cui viene chiamato  è sottotipo di `t`

  ​

Nel nostro compilatore abbiamo i seguenti tipi, definiti dalla `enum TypeID`:

- `VOID` - nessun tipo
- `BOOL` - un valore booleano
- `INT` - un valore intero
- `FUN` - una funzione o un metodo (seguono le stesse regole di typing)
- `CLASSDEC` - una classe
- `INSTANCE` - un'istanza di classe



Per fornire un esempio del funzionamento del type system in FOOL consideriamo il caso di applicazione di un operatore a due elementi. Come spiegato nel paragrafo 2.3.2, ogni operatore ha due `INode` figli che rappresentano l'elemento di destra e l'elemento di sinistra. 

1. Nel caso di operatori che confrontano valori interi il controllo andrà a verificare che sia il tipo dell'elemento destro sia dell'elemento sinistro destra sia sottotipo del tipo `INT`.

2. Nel caso invece di operatori booleani avverrà la medesima cosa ma accertandosi che entrambi siano sottotipi di BOOL

   ​

### 4.2 Subtyping

Il concetto di subtyping identifica un tipo T come sottotipo di un tipo S (i.e. `T <: S`) se è vera una delle seguenti affermazioni: 

- T ed S sono lo stesso tipo   (`T <: T`)
- T eredita da S   (`T <: S`)
- T eredita da un tipo U che eredita da S   (`T <: U && U <: S`)

Nel nostro compilatore l'analisi del subtyping diventa fondamentale con l'aggiunta delle classi e dell'**ereditarietà**, dei metodi e dell'**overriding**. Il type checking viene fatto anche per i tipi più semplici come booleani, interi tuttavia in questi casi il metodo `isSubtypeOf()` eseguirà solamente un confronto di uguaglianza fra i due tipi in questione poichè questi tipi non possono avere estensioni nel linguaggio FOOL.



#### 4.2.1 FunType

Questa classe si occupa di controllare il corretto sottotipaggio tra due metodi come descritto nella consegna:

> *"Il tipo di una funzione f1 è sottotipo del tipo di una funzione f2 se il tipo ritornato da f1 è sottotipo del tipo ritornato da f2, se hanno il medesimo numero di parametri, e se ogni tipo di paramentro di f1 è sopratipo del corrisponde tipo di parametro di f2."* 



Supponendo che siano definite le funzioni

-  `T' f1(T1', ... , Tn')` 
-  `T f2(T1, ... , Tn)`

allora la regola per il sottotipaggio tra funzioni è:


$$
\frac
{
T_1 <: T_1^{'} \quad \ldots \quad T_n <: T_n^{'} \qquad \&\& \qquad
T^{'} <: T
}
{
T_1^{'} \times \ldots \times T_n^{'} \rightarrow T^{'} 
\quad 	<:  \quad
T_1 \times \ldots \times T_n \rightarrow T 
} \quad [metOver]
$$


All'interno di `FunType.java` questa regola è implementata con il seguente metodo:

```java
public boolean isSubTypeOf(Type t) {
	if (t instanceof FunType) {
    	FunType funType = (FunType) t;
        boolean check = true;
        if (this.params.size() == funType.getParams().size()) {
            for (int i = 0; i < this.params.size(); i++) {  
              check &= funType.getParams().get(i)
                		.isSubTypeOf(this.params.get(i));
            }
            check &= this.returnType.isSubTypeOf(funType.returnType);
        } else {
            check = false;
        }
        return check;
    } else {
        return false;
    }
}
```

Notare come le chiamate ricorsive a `isSubtypeOf()` scandaglino la gerarchia dei tipi e come per il controllo del tipo di ritorno vengono invertiti i ruoli dei tipi chiamante e parametro.



#### 4.2.2 ClassType

Questa classe si occupa di controllare il corretto sottotipaggio tra due classi che, come descritto nella consegna, può avvenire se: 

1. > [Estensione **diretta**] *"Una classe C1 è sottotipo di una classe C2 se C1 estende C2 e se i campi e metodi che vengono sovrascritti sono sottotipi rispetto ai campi e metodi corrispondenti di C2...*

2. > [Estensione **indiretta**] *..Inoltre, C1 è sottotipo di C2 se esiste una classe C3 sottotipo di C2 di cui C1 è sottotipo."*

Supponendo che siano definite le due classi

- ```fool
  class A (t1 a1, ... , tn an) {
    t1' fa1(...)   
   	// exp with type t1'
    ... 
    tn' fan(...)   
   	// exp with type tn'
  } 
  ```

- ```
  class B (T1 b1, ... , Tn bn) {
    T1' fb1(...)   
   	// exp with type T1'
    ... 
    Tn' fbn(...)   
   	// exp with type Tn'
  }
  ```



allora la regola per il sottotipaggio tra due classi è:


$$
\frac
{
b1 <: a1 \quad \ldots \quad bn <: an
\qquad \&\& \qquad
fb1 <: fa1 \quad \ldots \quad fbn <: fan
}
{
A
\quad 	<:  \quad
B
} \quad [classExt]
$$
Questa regola, che al suo interno sfrutta anche la regola ==[metOver]== per il sottotipaggio di metodi, viene implementata nel metodo `checkSemantics` come precedentemente descritto nel punto 13 della sezione 3.2.3 di questa relazione. 



All'interno di `ClassType.java` viene implementato solo il controllo sull'estensione indiretta con il seguente metodo:

```java
public boolean isSubTypeOf(Type t2) {
    if (t2 instanceof ClassType) {
        ClassType ct2 = (ClassType) t2;
      	if (this.getClassID().equals(ct2.getClassID())) {
            return true;
        }
        if (superType != null) {
            return this.getSuperclassID().equals(ct2.getClassID()) ||
                   superType.isSubTypeOf(t2);
        }
    }
    return false;
}
```

Per prima cosa verifichiamo che le due classi non siano dello stesso tipo, ovvero abbiano lo stesso nome, in tal caso ritorniamo immediatamente **true**. Altrimenti, si procede a controllare se siamo in presenza di eredetarietà multilivello, ovvero se è la superclasse di `this` ad essere un estensione diretta di `t2`. Con il secondo costrutto *if* si risale ricorsivamente la catena di eredetarietà cercando `t2`.



#### 4.2.3 InstanceType

La classe che rappresenta il tipo istanza di una classe contiene al suo interno il `ClassType` della classe di cui è istanza. Il controllo sul subtyping avviene delegando al metodo `isSubTypeOf()` di `ClassType` 



## 5. Code generation

// TODO: scegliere cosa tenere della roba seguente

/* -----------------------------------------------------------------------------------------------------------------

### Code generation (classdec)

- Creare una lista `new_methods` che contiene `methods` meno `supertype.methods`
- Se `C` estende un'altra classe `Super`:
  - creare una nuova entry `c_entry` nella dispatch table copiando quella di `Super`
- Altrimenti:
  - creare una nuova entry `c_entry` vuota nella dispatch table
- Per ogni metodo `new_m` in `new_methods`:
  - creare una nuova label `new_m_label` per `new_m`
  - inserire `new_m_label` + `codegen(new_m)` nel codice delle funzioni
  - aggiungere in `c_entry` un nuovo metodo `new_m.id` con label `new_m_label`

------

## 2. Istanziazione di classe (oggetto)

### Descrizione

L'istanziazione di classe è definita in `NewNode` e si occupa di creare oggetti nello heap e restituirne l'indirizzo

### Attributi

| Attributo | Tipo               | Descrizione         |
| --------- | ------------------ | ------------------- |
| `classID` | `String`           | id della classe     |
| `args`    | `ArrayList<INode>` | lista dei parametri |

### Validazione semantica

- Recuperare il `ClassType` corrispondente a `classID` dalla tabella dei simboli
- Verificare che il numero di attributi passati al costruttore sia uguale a `classtype.fields.size()`

### Validazione di tipo

- Scorrere `args` con un indice `i = 0`:
  - verificare che `typecheck(args[i])` sia sottotipo di `typecheck(classtype.fields[i])`

$$
\frac{
	\Gamma \vdash A : \text{Class } \qquad \Gamma \vdash A.a_1 : T_1, \ldots, A.a_n : T_n \qquad  St_1 <: T_1 \ldots St_n <: T_n
}{
	\Gamma \vdash \text{new } A(a_1 : St_1 , \ldots , a_n : St_n ) : \text{Instance}
}
[new]
$$

- Allocare una nuova area di memoria nello heap
- Quando creo un oggetto di classe C, devo dargli un puntatore che punti alla dispatch table di classe C (TODO)
- TODO

------

## 3. Utilizzo di un attributo

### Descrizione

Gli attributi sono dei casi particolari di `IdNode` il cui valore non si trova in uno scope esterno ma in un oggetto nello heap 

### Attributi

| Nome                   | Tipo               | Descrizione                              |
| ---------------------- | ------------------ | ---------------------------------------- |
| `attrib_id`            | `String`           | L'id dell'attributo                      |
| `entry`                | `SymbolTableEntry` | Entry della definizione dell'attributo nell Symbol Table |
| `nesting_level`        | `Integer`          | Nesting level dell'utilizzo dell'attributo |
| `object_offset`        | `Integer`          | L'offset dell'oggetto rispetto al `frame pointer` |
| `object_nesting_level` | `Integer`          | Nesting level di definizione dell'oggetto |

### Validazione semantica

- TODO

### Validazione di tipo

- TODO

### Code generation

- TODO

------

## 4. Chiamata di metodo

### Descrizione

La chiamata ad un metodo è definita in `MethodCallNode` e, diversamente da una chiamata a funzione, deve mettere sullo stack un riferimento all'oggetto sul quale viene eseguita

### Attributi

| Nome                   | Tipo              | Descrizione                              |
| ---------------------- | ----------------- | ---------------------------------------- |
| `method_id`            | `String`          | L'id del metodo chiamato                 |
| `method_offset`        | `Integer`         | Offset del metodo nella dispatch table   |
| `args`                 | `ArrayList<Node>` | La lista degli argomenti passati al metodo |
| `nesting_level`        | `Integer`         | Nesting level della chiamata del metodo  |
| `object_id`            | `String`          | L'id dell'oggetto su cui è chiamato il metodo |
| `object_offset`        | `Integer`         | L'offset dell'oggetto rispetto al `frame pointer` |
| `object_nesting_level` | `Integer`         | Nesting level di definizione dell'oggetto |

### Validazione semantica

- Recupera dalla symbol table l' `object_type` di `object_id`
- Verifica che `object_type` sia di tipo `InstanceType`
- Verifica che il `class_type` di `object_type` contenga un metodo che abbia  `id` uguale a `method_id` e recupera `method_type`
- Verifica che il numero di `args` sia uguale al numero di parametri definito in `method_type`

### Validazione di tipo

- Scorre `args` con un indice `i = 0` verificando per ogni argomento il tipo di `args[i]` sia sottotipo di `method_type.args[i]`

### Code generation

- Carica il valore di `$fp` sullo stack
- Per ogni `a` in `args` inserisce sullo stack `codegen(a)`
- Carica sullo stack l'`object_offset` e risale la catena statica fino a caricare l'indirizzo dell'activation record dove è definito l'oggetto
- Somma i due valori precedenti ottenendo e caricando sullo stack l'`object_address`, l'indirizzo dell'oggetto nello heap
- Carica sullo stack l'`object_address` e `method_offset` decrementato di `1` perché //TODO
- Somma i due valori precedenti ottenendo e caricando sullo stack l'indirizzo del codice del metodo
- Setta `$ra` e salta all'esecuzione del codice del metodo

------

## 5. Dispatch tables

### Descrizione

Le dispatch tables sono implementate in `CodegenUtils` come strutture dati popolate durante la valutazione semantica, dalle quali successivamente si genera il codice `SVM` che viene aggiunto in fondo al risultato della code generation

### Attributi

Viene usata un'hashmap di liste `String => ArrayList<DispatchTableEntry>` che associa ad ogni chiave `class_id` una lista ordinata di `DispatchTableEntry`, delle coppie costituite da:

| Nome           | Tipo     | Descrizione                              |
| -------------- | -------- | ---------------------------------------- |
| `method_id`    | `String` | L'id del metodo chiamato                 |
| `method_label` | `String` | label corrispondente all'indirizzo del codice della funzione |

### Metodi

Per gestire le strutture dati sono disponibili: 

- `void addDispatchTable(String classID, ArrayList<DispatchTableEntry> dt)`
  Inserisce nella struttura dati la dispatch table `dt` per la classe `classID` 
- `ArrayList<DispatchTableEntry> getDispatchTable(String classID)`
  Restituisce una copia della dispatch table della classe `classID`, viene usato per il subtyping
- `String generateDispatchTablesCode()`
  Genera e restituisce il codice `SVM` delle dispatch tables

## 6. Esempi

- ### Esempio 1

```fool
class Calculator (
  int x
) {
  int xPlus(int i)
    x + i
  ;
}

class BetterCalculator implements Calculator (
  int x
) {
  int xPlusOne()
    xPlus(1)
  ;
}

class WrongCalculator implements Calculator (
  int x,
  int y
) {
  int xPlus(int i)
    y
  ;
}
```

- ### Dispatch tables

#### Calculator

| Offset | Value          |
| :----- | :------------- |
| 1      | label of xPlus |

#### BetterCalculator

| Offset | Value              |
| :----- | :----------------- |
| 1      | label of  xPlus    |
| 2      | label of  xPlusOne |

- #### WrongCalculator

| Offset | Value                     |
| :----- | :------------------------ |
| 1      | label of overridden xPlus |

- ### Heap layout

#### Calculator

`Calculator c = new Calculator(1)`

| Row no. | Value        |
| :------ | :----------- |
| 1       | `address(c)` |
| 2       | 1            |

#### BetterCalculator

`BetterCalculator bc = new BetterCalculator(2)`

| Row no. | Value         |
| :------ | :------------ |
| 1       | `address(bc)` |
| 2       | 2             |

#### WrongCalculator

`WrongCalculator wc = new WrongCalculator(7, 6)`

| Row no. | Value         |
| :------ | :------------ |
| 1       | `address(wc)` |
| 2       | 7             |
| 3       | 6             |

-------------------------------------------------------------------------------------------------------------------------- */



## 6. Stack Virtual Machine

Una volta generato il bytecode, questo viene eseguito da una **SVM** (Stack Virtual Machine). Questa macchina virtuale dispone di uno **stack**, che rappresenta la memoria della macchina; la computazione è espressa da ripetute modifiche allo stack tramite operazioni di **push** e **pop**.

La macchina virtuale richiede in input un parametro `int[] code`, un array contenente una serie di istruzioni definite nella grammatica `SVM.g4`. Queste vengono lette una ad una e, tramite un costrutto *switch-case* in `ExecuteVM.java`, per ognuna di loro è fornita una implementazione in termini di operazioni di push e pop sullo stack.

Rispetto all'implementazione iniziale della VM, la modifica più importante è l'introduzione di una operazione di **new**, usata per allocare un oggetto in memoria. Gli oggetti, che sono istanze delle classi, non possono risiedere sullo stack insieme al resto dei dati e per questo motivo la loro creazione non può essere definita tramite le classiche operazioni di push e pop.

Per risolvere questo problema l'operazione `new` alloca gli oggetti nella parte più alta dello stack (indirizzi bassi), in un'area denominata **heap**.

### 6.1 Heap

Lo heap è implementato tramite una lista libera e rende disponibili i metodi:

- `HeapMemoryCell allocate(int size) throws VMOutOfMemoryException`
  - alloca un'area di memoria, rimuovendo dalla lista libera `size` elementi, e restituisce al chiamante il primo elemento rimosso. Da questo è possibile accedere agli elementi successivi tramite il suo attributo `next`
  - nel caso la memoria richiesta sia superiore a quella disponibile, viene lanciata un'eccezione
- `void deallocate(HeapMemoryCell firstCell)`
  - dealloca la memoria il cui primo blocco viene passato come parametro e la reinserisce nella lista libera, in modo che torni ad essere disponibile per l'allocazione

E' stato scelto di implementare lo heap con una lista libera in modo da facilitare la gestione della **garbage collection**, infatti dopo una serie di allocazioni e deallocazioni di dimensioni differenti, lo heap potrebbe presentare *frammentazione interna*. L'uso della lista libera permette alla VM di ottenere blocchi di memoria logicamente, ma non fisicamente, contigui; in modo che essa possa operare senza tenere conto di questo problema.

### 6.2 Garbage Collection

E' stato realizzato un garbage collector usando la tecnica **mark and sweep**: se l'indirizzo di un oggetto non viene trovato nello stack o nel registro *RV*, allora tale oggetto può essere deallocato. Usando semplici numeri interi per rappresentare l'indirizzo di un oggetto, la ricerca di un indirizzo attivo sullo stack può produrre *falsi positivi*, poichè l'intero trovato potrebbe non fare riferimento all'oggetto ma ad un semplice valore numerico. Per ridurre questa probabilità è stato introdotto un offset (`MEMORY_START_ADDRESS`) con un valore elevato per indicare l'inizio della memoria, in quanto ad esempio il numero 0 (che è sempre presente come primo valore sullo stack), o più in generale numeri bassi, sono più facilmente trovabili in programmi comuni (p.e si pensi ad un iteratore).

L'operazione di garbage collection viene eseguita se prima di allocare un oggetto, la differenza tra `sp` e `hp` e' minore o uguale al massimo tra:

- 5% della memoria totale
- 10 (in caso di memoria particolarmente piccola)

## 7. Testing e conclusioni

Se si ha voglia, sarebbe carino con qualche programma FOOL fare un albero come quello a pagina 68 della slide 6 di Laneve...