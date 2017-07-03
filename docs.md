# FOOL - Functional Object Oriented Language

#### Progetto del corso di Compilatori ed Interpreti AA 2016/2017

##### Corso di Laurea Magistrale in Informatica, Università di Bologna

##### ***Componenti del gruppo*** (in ordine alfabetico)

- Alberto Nicoletti (matricola 819697)
- Devid Farinelli (matricola 819683)
- Mirco Civolani (matricola 798717)
- Pietro Battilana (matricola 799486)

------

## **Tabella dei contenuti**

[TOC]

## 1. Struttura del progetto

Il progetto del corso prevede l'implementazione di un compilatore per codice sorgente FOOL ed un interprete che prendendo il codice intermedio, generato dal compilatore, lo traducesse in istruzioni eseguibili direttamente dal calcolatore. Il processore finale viene simulato dalla classe `ExecuteVM.java` che recuperando le istruzioni dal codice e modificando i valori delle memoria produce il risultato voluto.



L'intero progetto è sviluppato in Java utilizzando l'IDE IntelliJ IDEA e le librerie di ANTLR v4.7.  La cartella `src` contiene il codice sorgente che è suddiviso in diversi package

- `exception`

  contiene le classi necessarie ad istanziare le eccezzioni sintattiche, semantiche e a run-time con i corretti messaggi d'errore.

- `grammar`

  contiene le grammatiche del linguaggio FOOL e del linguaggio dell'interprete nel formato *.g4* di ANTLR che a partire da queste produce una serie di classi (i.e. lexer e costruzione AST)

- `main`

  contiene la nostra implementazione della classe per la la costruzione per visita dell'albero sintattico astratto. Inoltre contiene le classi che avviano e configurano l'intero processo di compilazione-interpretazione-esecuzione su un singolo input oppure sulla test unit *test.yml*

- `node`

  contiene una classe per ogni nodo dell'AST creato dal lexer. Ognuno di questi nodi contiene i metodi il controllo semantico, la generazione di codice (visita con modalità top-down) e di tipo (visita con modalità bottom-up). 

- `symbol_table`

  contiene la tabella dei simboli, implementata con una lista di hashtable, necessaria nella fase di dichiarazione e di referenza ad una variabile e nella fase di controllo semantico.

- `type`

  contiene le classi corrispondenti ai tipi forniti dal linguaggio FOOL. Le istanze di queste classi, con tutte le informazioni necessarie, vengono memorizzate all'interno della tabella dei simboli che ha visibilità globale.

- `util`

  contiene metodi di utilità utilizzati nella fase di code generation, infatti è quì che viene creata e gestita la dispatch table necessaria agli oggetti a run-time.

- `vm`

  contiene le classi che simulano l'archittetura e l'instruction set di un calcolatore dotato di una memoria gestita in parte come stack e in parte come heap.

Sono state realizzate **entrambe** le richieste opzionali nella consegna del progetto, ovvero garbage collection e estensioni con gli operatori (`<`, `>`, `<=`, `>=`, `||`, `&&`, `/`, `-`,  `!`).

### 1.1 Installazione ed esecuzione

Spiegare le modalità per importare e eseguire il progetto ... TODO

## 2. Analisi lessicale e sintattica

In questa sezione discuteremo delle grammatiche definite per il linguaggio FOOL e per il linguaggio SVM. In particolare ci soffermeremo sulle parti delle grammatiche modificate che riguardano le funzionalità aggiunte rispetto ai linguaggi forniti nella consegna.

### 2.1 Grammatica FOOL

Non è stato necessario modificare la produzione iniziale `prog` del linguaggio che può essere una semplice espressione oppure un espressione preceduta da dichiarazioni di variabili `let in` o di classi. Si è scelto di usare il non terminale `met`  per la definizione di metodi che per gestirli diversamente successivamente a livello semantico rispetto alla definizione di funzioni. Come è possibile vedere a riga 10, `met` è solo un wrapper per il non terminale `fun`. 

```ANT
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



Si è deciso di unificare a livello sintattico la chiamata di funzione con la chiamata di metodo come fatto per la loro definizione. Il non terminale corrispondente è chiamato `funcall`. Una espressione `exp` si può ridurre ad un `value` le cui produzioni sono distinte attraverso le direttive che iniziano con '\#' in `funExp` per la chiamata di funzione ed in `methodExp` per la chiamata di metodi. Queste due direttive ci permettono di scrivere due procedure diverse (`visitFunExp` e `visitMethodExp`) per la visita dei relativi nodi nell'albero di sintassi astratta.

```ANTLR
value
    :  ...
    | funcall       							#funExp
    | (ID | THIS) DOT funcall                   #methodExp 
    |  ... ;

funcall : ID ( LPAR (exp (COMMA exp)* )? RPAR ) ;
```



L'implementazioni degli operatori aggiuntivi a livello sintattico è stata fatta in modo molto semplice aggiungendo delle opzioni per la riduzione del non terminale `operator`.  

```ANTLR
exp :  ('-')? left=term (operator=(PLUS | MINUS) right=exp)? ;

term : left=factor (operator=(TIMES | DIV) right=term)? ;

factor : 
left=value (operator=(AND | OR | GEQ | EQ | LEQ | GREATER | LESS) right=value)? ;
```



### 2.2 Grammatica SVM

È stato necessario apportare modifiche anche alla *attribute grammar* dell'interprete FOOL. Per la operazioni di sottrazione e divisione sono stati semplicemente aggiungi i relativi terminali e non terminali `sub` e `div`. Invece per quanto riguarda l'operazione di `<=` è stata aggiunta una regola `BRANCHLESSQ` che si comporta in modo simile alla regola di `<` aggiungendo una *label* nel codice e alla collezione `labelRef` usata per fare *backpatching* alla fine della fase di parsing. È stata introdotta invece una nuova istruzione `LC` che come viene implementata come mostrato di seguito.

```java
case SVMParser.LC:
	int codeAddress = pop();
    push(code[codeAddress]);
	break;
```

come si può vedere svolge un operazione molto simile a `LOADW`, ovvero prende l'indirizzo in cima allo stack e con questo accede all'array `code`, infine carica sullo stack il valore ottenuto. Questa nuova istruzione è utilizzata nella chiamata ad un metodo ed il valore ottenuto da `LC` è la prima istruzione di tale metodo a cui si salta con l'operazione di `JS`.



Alla grammatica dell'interprete inoltre è stata aggiunta un istruzione chiamata `HOFF` che sta per 'heap offset' utilizzato nella referenza ad un campo di un oggetto. Il suo scopo è quello di convertire l'offset del campo di un oggetto nell'offset reale tra l'inizio dell'oggetto nello heap ed il valore di questo campo. 

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

La struttura ed il funzionamento dello heap dove vengono memorizzate le istanze di oggetti verrà discussa successivamente.  In questo esempio, come in altre parti del progetto, vengono utilizzati i metodi `stream` di Java 8 per lavorare su collezzioni in modo compatto senza usare cicli.



Si è resa la dimensione dell'array `code`, contenente il bytecode, variabile a seconda del codice SVM prodotto dal compilatore FOOL. Ciò è stato fatto cambiando l'array `int[] code` nella sezione annotata come *@parser:members* in un private `ArrayList<Integer> code` di dimensioni inizialmente nulle.  Nelle regole per l'*assembly* per aggiungere un istruzione si chiama `code.add(instruction_int_code)`. In tal modo il codice sarà lungo esattamente quanto necessario senza sprechi di memoria. Si è modificato leggermente di conseguenza anche il *backpatching* per accedere ad ArrayList. 



### 2.3 Classi Node

Le componenti di un programma FOOL vengono legate a delle classi nodo; queste implementano il comportamento corretto delle varie produzioni della grammatica nelle diverse fasi di compilazione. L'interfaccia `INode` contiene le definizioni dei metodi necessari e viene implementata da tutti gli altri nodi. A questa interfaccia abbiamo scelto di rimpiazzare il metodo `toPrint()` con quello nativo di Java  `toString()` per la stampa dell'intero albero AST. Per fare questo abbiamo aggiunto un metodo `getChilds()` che restituisce un `ArrayList<INode>`  con i figli del nodo attuale.

#### 2.3.2 Nodi operatore

La grammatica inizialmente permetteva solamente l'utilizzo dell'operatore `==`, che rendeva il linguaggio davvero limitato. Abbiamo quindi scelto di adempiere alla richiesta opzionale di aggiungere `<=`, `>=`, `<`, `>` per i confronti fra interi,  `&&`, `||`  per i confronti fra booleani ed anche gli operatori di divisione, sottrazione e il NOT (i.e. `!`).

Ogni nodo operatore (escluso il NOT) presenta gli stessi parametri, ovvero:

- `INode left` :  l'elemento a sinistra dell'operazione
- `INode right`:  l'elemento a destra dell'operazione

mentre il nodo operatore NOT ha solamente un `INode` figlio che è il booleano su cui si sta applicando il NOT.





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

In seguito all'analisi semantica, viene eseguito il type checking del programma FOOL in input. Il controllo dei tipi viene però svolto in ordine bottom-up rispetto ai nodi dell'AST. Ogni `INode` presenta un metodo `type()` che applica le regole di inferenza definite in seguito ed in caso esse vengano verificate, restituisce il tipo di quel nodo, altrimenti viene lanciata un'eccezione indicando il tipo di errore.



Il tipo dell'intero AST, ritornato dal metodo `type()` della radice, è il tipo della espressione `exp` finale del programma FOOL.

### 4.1 Type system

Durante il controllo dei tipi si va a controllare che, per un determinato nodo, l'operazione, la funzione o la classe, o più in generale tutti i componenti di quel nodo rispettino le regole di inferenza. Nella nostra implementazione Java, abbiamo scelto di creare un'interfaccia `Type` che presenta i seguenti metodi su cui basare le regole di typing: 

- `String getID()` - restituisce un valore dell'enumerazione `TypeID` 

- `boolean isSubtypeOf(Type t)` - restituisce `true` se la classe tipo da cui viene chiamato è sottotipo di `t`, `false` altrimenti

  ​

Nel nostro compilatore abbiamo i seguenti tipi, definiti dalla `enum TypeID` in seguito elencata:

- `BOOL` - un valore booleano:


$$
\frac{}{\Gamma \vdash true : Bool}[BoolTrue]
\qquad \qquad 
\frac{}{\Gamma \vdash false : Bool}[BoolFalse]
$$

$$
\frac{\Gamma \vdash e_1 : Bool \qquad \Gamma \vdash e_2 : Bool}{\Gamma \vdash e_1 \ \&\& \ e_2 : Bool}[And]
\qquad
\frac{\Gamma \vdash e_1 : Bool \qquad \Gamma \vdash e_2 : Bool}{\Gamma \vdash e_1 \ || \ e_2 : Bool}[Or]
$$

$$
\frac{\Gamma \vdash e_1 : Bool \qquad \Gamma \vdash e_2 : Bool}{\Gamma \vdash e_1 \ < \ e_2 : Bool}[Less]
\qquad
\frac{\Gamma \vdash e_1 : Bool \qquad \Gamma \vdash e_2 : Bool}{\Gamma \vdash e_1 \ > \ e_2 : Bool}[Greater]
$$

$$
\frac{\Gamma \vdash e_1 : Bool \qquad \Gamma \vdash e_2 : Bool}{\Gamma \vdash e_1 \ <= \ e_2 : Bool}[LessEqual]
\qquad
\frac{\Gamma \vdash e_1 : Bool \qquad \Gamma \vdash e_2 : Bool}{\Gamma \vdash e_1 \ >= \ e_2 : Bool}[GreaterEqual]
$$

$$
\frac{\Gamma \vdash c : Bool \qquad e_1 : T_1 \qquad e_2 : T_2 \qquad T_1 <: T \qquad T_2 <: T}{\Gamma \vdash if \ \ c \ \ then \ \ \{e_1\} \ \ else \ \ \{e_2\} : T}[IfThenElse]
$$

- `INT` - un valore intero:


$$
\frac{\text{x is an} \ \ Int \ \ \text{token}}{\Gamma \vdash x : Int}[Int]
$$

$$
\frac{\Gamma \vdash e_1 : Int \qquad \Gamma \vdash e_2 : Int}{\Gamma \vdash e_1 \ + \ e_2 : Int}[Sum]
\qquad
\frac{\Gamma \vdash e_1 : Int \qquad \Gamma \vdash e_2 : Int}{\Gamma \vdash e_1 \ - \ e_2 : Int}[Sub]
$$

- `FUN` - una funzione o un metodo (seguono le stesse regole di typing):


$$
\frac{\Gamma [p_1 \rightarrow T_1] \ ... \ [p_n \rightarrow T_n] \vdash e : T}{\Gamma \vdash T \ foo(T_1 \ p_1, ..., T_n \ p_n) \ e; \  : (T_1, ..., T_n) \rightarrow T}[FunDef]
$$

$$
\frac{\Gamma \vdash foo : (T_1, ..., T_n) \rightarrow T \qquad a_1 : ST_1 <: T_1 ,\ldots, a_n : ST_n <: T_n}{\Gamma \vdash foo(ST_1 \ a_1,...,ST_n \ a_n) : T}[FunApp]
$$

- `CLASSDEC` - una classe:


$$
\frac{\Gamma [a_1 \rightarrow T_1]...[a_n \rightarrow T_n][f_1p_1 \rightarrow F_1T_1] \ ... \ [f_1p_n \rightarrow F_1T_n] \vdash e_1 : F_1 \ ... \ \Gamma [a_1 \rightarrow T_1]...[a_n \rightarrow T_n][f_np_1 \rightarrow F_nT_1] \ ... \ [f_np_n \rightarrow F_nT_n] \vdash e_n : F_n}{\Gamma \vdash class \ A \ (T_1 \ a_1, ..., T_n \ a_n) \ \{F_1 \ f_1(F_1T_1 \ f_1p_1, ..., F_1T_n \ f_1p_n) \ e_1;,...,F_n \ f_n(F_nT_1 \ f_np_1, ..., F_nT_n \ f_np_n) \ e_n;\} : Class}[ClassDef]
$$

- `INSTANCE` - un'istanza di classe:



$$
\frac{
	\Gamma \vdash A : Class \qquad \Gamma \vdash A.a_1 : T_1, \ldots, A.a_n : T_n \qquad  a_1 : ST_1 <: T_1 ,\ldots, a_n : ST_n <: T_n
}{
	\Gamma \vdash \text{new } A(ST_1 \ a_1, \ldots ,ST_n \ a_n) : Instance
}
[New]
$$

$$
\frac{\Gamma \vdash o : Instance \quad \exists \ foo : (T_1,...,T_n) \rightarrow T \in methods(class(o)) \ | \ a_1 : T'_1,...,a_n : T'_n \quad T'_1 <: T_1 \ ... \ T'_n <: T_n}{\Gamma \vdash o.foo(a_1,...,a_n) : T}[MethodCall]
$$

In aggiunta a queste regole, e' stato anche definito l'operatore **let-in** per permettere l'introduzione di variabili all'interno di un'espressione:
$$
\frac{\Gamma[x \rightarrow T''] \vdash e' : T' \quad e:T <: T''}{\Gamma \vdash let \ T'' \ x \ = \ e \ in \ e' \ : T'}[LetIn]
$$

### 4.2 Subtyping

Si considerano in seguito le regole di subtyping per i tipi definiti precedentemente.

#### 4.2.1 Interi

$$
\frac{\Gamma \vdash n_1 : Int \quad n_2 : Int}{\Gamma \vdash n_1 <: n_2}[IntSubtype]
$$

#### 4.2.2 Booleani

$$
\frac{\Gamma \vdash b_1 : Bool \quad b_2 : Bool}{\Gamma \vdash b_1 <: b_2}[BoolSubtype]
$$

#### 4.2.1 Funzioni

La regola di subtyping per le funzioni e':


$$
\frac
{
\Gamma \vdash f_1 : (T_1^{'} , \ldots , T_n^{'}) \rightarrow T^{'}
\qquad \Gamma \vdash f_2 : (T_1 , \ldots , T_n) \rightarrow T 
\qquad T_1 <: T_1^{'} \ \ldots \ T_n <: T_n^{'}
\qquad T^{'} <: T
}
{
\Gamma \vdash f_1<: f_2 
}[FunSubtype]
$$


Questa regola viene implementata all'interno del file `FunType.java`.

#### 4.2.2 Classi

Le regole di subtyping tra classi sono:

$$
\frac
{
\Gamma \vdash A : Class \quad B:Class
\quad B \ \ implements \ \ A
\quad \forall \ a_i \in fields(A) ,\ \exists \ b_i \in fields(B) \ | \ b_i <: a_i
\quad \forall fb_i \in redefined\_methods(B), \ \exists \ fa_i \in methods(A) \ | fb_i <: fa_i
}
{
B <: A
} [ClassDirectSubtype]
$$
$$
\frac
{\Gamma \vdash A : Class \quad C : Class \qquad \exists \ B :Class \ | \ C <: B \quad B <: A}
{\Gamma \vdash C <: A}[ClassIndirectSubtype]
$$

Queste regole di subtyping vengono implementate all'interno del file `ClassType.java`.

#### 4.2.3 Istanze

La regola di subtyping tra istanze di classi e':
$$
\frac{\Gamma \vdash a : Instance \quad b : Instance \quad class(a) <: class(b)}{\Gamma \vdash a <: b}[InstanceSubtype]
$$
Questa regola di subtyping e' implementata nel file `InstanceType.java`.

## 5. Code generation

### 5.1 Class definition

- Creare una lista `new_methods` che contiene `methods` meno `supertype.methods`
- Se `C` estende un'altra classe `Super`:
  - creare una nuova entry `c_entry` nella dispatch table copiando quella di `Super`
- Altrimenti:
  - creare una nuova entry `c_entry` vuota nella dispatch table
- Per ogni metodo `new_m` in `new_methods`:
  - creare una nuova label `new_m_label` per `new_m`
  - inserire `new_m_label` + `codegen(new_m)` nel codice delle funzioni
  - aggiungere in `c_entry` un nuovo metodo `new_m.id` con label `new_m_label`

### 5.2 Method call

- Carica il valore di `$fp` sullo stack
- Per ogni `a` in `args` inserisce sullo stack `codegen(a)`
- Carica sullo stack l'`object_offset` e risale la catena statica fino a caricare l'indirizzo dell'activation record dove è definito l'oggetto
- Somma i due valori precedenti ottenendo e caricando sullo stack l'`object_address`, l'indirizzo dell'oggetto nello heap
- Carica sullo stack l'`object_address` e `method_offset` decrementato di `1` perché //TODO
- Somma i due valori precedenti ottenendo e caricando sullo stack l'indirizzo del codice del metodo
- Setta `$ra` e salta all'esecuzione del codice del metodo

### 5.3 Istanziazione di classe

TODO

### 5.4 Utilizzo di un attributo

TODO

### 5.5 Dispatch table

La dispatch table e' implementata in `CodegenUtils` come struttura dati popolata durante la valutazione semantica, dalle quale successivamente si genera il codice `SVM` che viene aggiunto in fondo al risultato della code generation.

Viene usata un'hashmap di liste `String => ArrayList<DispatchTableEntry>` che associa ad ogni chiave `class_id` una lista ordinata di `DispatchTableEntry`, delle coppie costituite da:

| Nome           | Tipo     | Descrizione                              |
| -------------- | -------- | ---------------------------------------- |
| `method_id`    | `String` | L'id del metodo chiamato                 |
| `method_label` | `String` | label corrispondente all'indirizzo del codice della funzione |

Per gestire le strutture dati sono disponibili i metodi: 

- `void addDispatchTable(String classID, ArrayList<DispatchTableEntry> dt)`
  Inserisce nella struttura dati la dispatch table `dt` per la classe `classID` 
- `ArrayList<DispatchTableEntry> getDispatchTable(String classID)`
  Restituisce una copia della dispatch table della classe `classID`, viene usato per il subtyping
- `String generateDispatchTablesCode()`
  Genera e restituisce il codice `SVM` delle dispatch tables

TODO dire qualcosa di piu'

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

## 7. Testing

Durante lo sviluppo e' stato adottato un processo Test Driven Development (**TDD**) in modo da evitare che con cambiamenti al codice sorgente si "rompessero" feature gia' funzionanti.

Nello specifico e' stata creata una test suite dentro al file `test.yml`, il quale, adottando la sintassi YAML, presenta la seguente struttura:

```yaml
testId - descrizione del test:
-	codice fool
-	risultato atteso
```

Il file viene parsato e da ogni test viene estratto il codice fool, il quale viene eseguito e viene confrontato il risultato ottenuto con quello atteso. Se i due risultati sono diversi, il test viene segnato come fallito. Al termine dell'esecuzione di tutti i test viene indicato quanti di essi sono stati superati con successo.

Il codice originario non si prestava bene a questo tipo di procedimento, infatti non c'era modo ne' di ottenere il risultato finale di un'esecuzione, ne' di ottenere un errore di type checking, in quanto erano inseriti dei `System.exit()` in caso di errori. E' stato quindi eseguito un refactoring del codice inserendo una eccezione al metodo `type`, ed e' stato fatto in modo che il metodo `cpu` della VM restituisca un valore al termine dell'esecuzione.

## 8. Conclusioni

TODO Se si ha voglia, sarebbe carino con qualche programma FOOL fare un albero come quello a pagina 68 della slide 6 di Laneve...