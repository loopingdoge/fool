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

Il progetto del corso prevede l'implementazione di un compilatore per codice sorgente `FOOL` che generi delle istruzioni `SVM` le quali vengano eseguite su un calcolatore emulato. 

Sono state realizzate **entrambe** le richieste opzionali nella consegna del progetto, ovvero garbage collection e le estensioni con gli operatori (`<`, `>`, `<=`, `>=`, `||`, `&&`, `/`, `-`,  `!`).

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

  contiene metodi di utilitá principalmente usati in fase di code generation per la creazione di label e la generazione delle dispatch tables

- `vm`

  contiene le classi che emulano l'archittetura e l'instruction set di un calcolatore dotato di una memoria gestita in parte come stack e in parte come heap

### 1.1 Installazione ed esecuzione

#### Installazione

Le modalità per importare il progetto in Eclipse sono semplici e prevedono tre passaggi:

1. Scompattare l'archivio .zip contenente il progetto;
2. Su Eclipse, andare su 'File' -> 'Open Projects from File System...'
   1. Nella schermata successiva cliccare sul pulsante 'Directory' e selezionare la cartella 'fool' appena scompattata e cliccare Finish.
3. Fare click destro sul progetto quindi andare alla voce 'Build Path -> Configure Build Path'. Nella schermata che appare andare nel tab 'Libraries' e cliccare sul bottone 'Add External JARs', quindi selezionare tutti e 3 i file .Jar presenti nella cartella 'fool/libs'. Cliccare quindi 'Apply and Close'.

#### Esecuzione

Nel nostro progetto abbiamo due possibili file da poter eseguire, entrambi si trovano in 'fool/src/main', per eseguirli basta cliccare col tasto destro su di essi ed andare alla voce 'Run As' -> 'Java Application' e sono:

1. TestDebug.java:

   Prende in input il codice presente nel file 'fool/input.fool' e stampa nella console l'albero AST del codice, il byteCode ed il risultato;

2. TestComplete.java:

   Prende in input tutti gli esempi di codice che si trovano all'interno del file 'fool/test.yml' e nella console vengono stampati tutti gli esiti per ogni test, confrontando l'esito ottenuto con quello previsto(corretto). Al termine dell'esecuzione di ogni test viene stampato quanti test hanno avuto esito positivo sul totale.

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

Queste due etichette permettono di gestire i nodi separatamente durante l'analisi lessicale implementando logiche diverse nei metodi `visitFunExp` e `visitMethodExp`:

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

Per semplificare il riutilizzo dei valori sullo stack è stata aggiunta l'istruzione `COPY` che duplica il valore in cima alla pila. L'istruzione è implementata come segue:

```ANTLR
| COPY                        {   code.add(COPY);     }
```

#### 2.2.1 l = LABEL

Per la generazione del codice delle dispatch tables delle varie classi è stata aggiunta l'istruzione `LABEL` (non seguita dal terminale `COL`):

```antlr
| l=LABEL  {   labelRef.put(code.size(), $l.text);   code.add(0);    }
```

Questa istruzione inserisce in fondo all'array `code` l'etichetta che corrisponde al metodo dell'oggetto del quale stiamo generando la dispatch table

#### 2.2.2 LC

Per implementare le chiamate di funzioni è stata aggiunta l'istruzione `LC` che permette di ottenere l'indirizzo del codice di una funzione partendo dalla sua label:

```ANTLR
| LC                          {   code.add(LC);       }
```

L'istruzione `LC` si aspetta in cima allo stack la label di una funzione, rimuove il valore dalla pila e lo usa come indice nell'array `code`, pushando il valore ottenuto in cima allo stack:

#### 2.2.4 NEW

Serve ad allocare un'area di memoria nello heap e riempirla con i valori dei campi dell'oggetto che si vuole istanziare:

```antlr
| NEW                         {   code.add(NEW);      }
```

Questa istruzione si aspetta di avere in cima allo stack il numero di campi ed i rispettivi valori. Dopo averli recuperati alloca la memoria e la popola facendo garbage collection se necessario. La prima locazione di memoria occupata contiene l'indirizzo della dispatch table.

#### 2.2.5 HOFF

L'istruzione `HOFF` (heap offset) converte l'offset del campo di un oggetto nell'offset reale tra l'indirizzo dell'oggetto nello heap e l'indirizzo del campo. Viene utilizzato accedendo ad un campo per gestire il caso in cui gli oggetti siano memorizzati in celle non contigue di memoria:

```ANTLR
| HOFF                        {   code.add(HOFF);     }
```

### 2.3 Nodi

Ad ogni nodo dell'albero sintattico corrisponde una classe che implementa l'interfaccia `INode` che rispetto alla versione originale è stata modificata come segue:

- Il metodo `toPrint()` è stato sostituito dal metodo `toString()` nativo di Java
- Per poter stampare l'AST è stato aggiunto il metodo `ArrayList<INode> getChilds()` che restituisce i figli del nodo attuale

#### 2.3.1 Nodi operatore

La grammatica inziale permetteva definiva solamente l'operatore `==`, è stata quindi estesa per supportare anche:

- gli operatori sottrazione e divisione `-` e `/`


- gli operatori per il confronto fra interi  `<=`, `>=`, `<` e `>` 
- gli operatori booleani  `&&`, `||` e `!` 

Ogni nodo operatore (escluso il NOT) presenta ha due attributi:

| Campo   | Tipo    | Descrizione         |
| ------- | ------- | ------------------- |
| `left`  | `INode` | l'operando sinistro |
| `right` | `INode` | l'operando destro   |

mentre il nodo operatore NOT ha solamente un `INode` figlio che è il `BoolNode` su cui viene applicato.

#### 2.3.2 Nodi classe

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

## 3. Analisi semantica

### 3.1 Symbol Table

La tabella dei simboli fa parte dell'ambiente, un'istanza della classe `Environment` che viene passata ad ogni nodo dell'AST per eseguire l'analisi semantica. La tabella dei simboli è implementata con una lista di hashtable:

```java
private ArrayList<HashMap<String, SymbolTableEntry>> symbolTable
```

Dove `SymbolTableEntry` è una classe che ha come attributi:

| Campo          | Tipo      | Descrizione                              |
| -------------- | --------- | ---------------------------------------- |
| `nestingLevel` | `int`     | livello di nesting al quale si trova la entry |
| `type`         | `Type`    | tipo della entry                         |
| `offset`       | `int`     | offset della entry rispetto all'area di memoria in cui è definita |
| `isAttribute`  | `boolean` | indica se la entry è stata definita come attributo di una classe |

All'ambiente sono stati aggiunti anche diversi metodi per gestire le symbol table:

- `public Environment pushHashMap()`

  aggiunge alla lista una nuova HashMap, viene usato quando si visita un nuovo scope

- `public Environment popHashMap()`

  rimuove l'ultima HashMap aggiunta, viene usato all'uscita da uno scope

- `public Environment addEntry(String id, Type type, int offset)`

  inserisce nell'hashmap piú recente la chiave `id` con associata una `SymbolTableEntry` con tipo `type` ed offset `offset`. Se é giá presente lancia una `RedeclaredVarException`

- `public Environment setEntryType(String id, Type newtype, int offset)`

  serve per aggiornare l'attributo `type` della `SimbolTableEntry` con chiave `id`. Se non trova la chiave `id` lancia `UndeclaredClassException`. Poiché è possibile stabilire la struttura gerarchica fra classi solo in seguito alla visita di tutte le `classdec`, vengono inserite nella symbol table informazioni incomplete, questo metodo viene usato per aggiornare le informazioni sul supertipo di una classe.

- `public int getNestingLevel()`

  restituisce il valore di nesting level corrente

- `public SymbolTableEntry getLatestEntryOf(String id)`

  scorre la lista di `HashTables` e ritorna la entry con chiave `id` se trovata, altrimenti lancia una `UndeclaredVarException` 

- `public SymbolTableEntry getLatestEntryOfNotFun(String id)`

  come il metodo precedente ma ignora le entry di tipo `FunType`

- `public Type getTypeOf(String id)`

  come i metodi precedenti ma restituisce solo il `type` della entry

- `public SymbolTableEntry getLatestClassEntry()`

  restituisce la entry dell'ultima classe visitata, viene usato durante il controllo semantico nel caso di chiamata di un metodo usando `this`

### 3.2 Dichiarazioni di classi

Nel caso vi siano delle dichiarazioni di classi, la radice dell'AST sarà un `ProgClassDecNode` e l'analisi semantica visiterà per prima cosa tutti i `ClassNode` . Durante questo passaggio per ogni classe viene inserita una entry nella symbol table.

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

In seguito all'analisi semantica, viene eseguito il type checking del programma FOOL in input. Il controllo sui tipi viene però svolto in ordine bottom-up rispetto ai nodi dell'AST. Ogni `INode` presenta un metodo `type()` che applica le regole di inferenza definite in seguito ed in caso esse vengano verificate, restituisce il tipo di quel nodo, altrimenti viene lanciata un'eccezione indicando il tipo di errore.

Il tipo dell'intero AST, ritornato dal metodo `type()` della radice, è il tipo della espressione `exp` finale del programma FOOL.

### 4.1 Type system

Durante il controllo dei tipi si va a controllare che, per un determinato nodo, l'operazione, la funzione o la classe, o più in generale tutti i componenti di quel nodo rispettino le regole di inferenza. Nella nostra implementazione Java, abbiamo scelto di creare un'interfaccia `Type` che presenta i seguenti metodi su cui basare le regole di typing: 

- `String getID()` - restituisce un valore dell'enumerazione `TypeID` 

- `boolean isSubtypeOf(Type t)` - restituisce `true` se la classe tipo da cui viene chiamato è sottotipo del tipo passato come parametro `t`, `false` altrimenti

  ​

Nel nostro compilatore abbiamo i seguenti tipi, definiti dalla `enum TypeID` in seguito elencata:

- `BOOL` - un valore booleano:


$$
\frac{}{\vdash true : Bool}[BoolTrue]
\qquad
\frac{}{\vdash false : Bool}[BoolFalse]
\qquad
\frac{\vdash e : Bool}{\vdash ! e}[Not]
$$

$$
\frac{\Gamma \vdash e_1 : Bool \qquad \Gamma \vdash e_2 : Bool}{\Gamma \vdash e_1 \ \&\& \ e_2 : Bool}[And]
\qquad
\frac{\Gamma \vdash e_1 : Bool \qquad \Gamma \vdash e_2 : Bool}{\Gamma \vdash e_1 \ || \ e_2 : Bool}[Or]
$$

$$
\frac{ e_1 : T_1 \qquad e_2 : T_2 \qquad T_1 <: T \qquad T_2<:T}{\Gamma \vdash e_1 \ == \ e_2 : Bool}[Equal]
$$

$$
\frac{\Gamma \vdash e_1 : Int \qquad \Gamma \vdash e_2 : Int}{\Gamma \vdash e_1 \ <= \ e_2 : Bool}[LessEqual]
\qquad
\frac{\Gamma \vdash e_1 : Int \qquad \Gamma \vdash e_2 : Int}{\Gamma \vdash e_1 \ >= \ e_2 : Bool}[GreaterEqual]
$$

$$
\frac{\Gamma \vdash e_1 : Int \qquad \Gamma \vdash e_2 : Int}{\Gamma \vdash e_1 \ < \ e_2 : Bool}[Less]
\qquad
\frac{\Gamma \vdash e_1 : Int \qquad \Gamma \vdash e_2 : Int}{\Gamma \vdash e_1 \ > \ e_2 : Bool}[Greater]
$$

$$
\frac{\Gamma \vdash c : Bool \qquad e_1 : T_1 \qquad e_2 : T_2 \qquad T_1 <: T \qquad T_2 <: T}{\Gamma \vdash if \ \ (c) \ \ then \ \ \{e_1\} \ \ else \ \ \{e_2\} : T}[IfThenElse]
$$



- `INT` - un valore intero:


$$
\frac{\text{x is an} \ \ Int \ \ \text{token}}{\vdash x : Int}[Int]
$$

$$
\frac{\Gamma \vdash e_1 : Int \qquad \Gamma \vdash e_2 : Int}{\Gamma \vdash e_1 \ + \ e_2 : Int}[Sum]
\qquad
\frac{\Gamma \vdash e_1 : Int \qquad \Gamma \vdash e_2 : Int}{\Gamma \vdash e_1 \ - \ e_2 : Int}[Sub]
$$



- `FUN` - una funzione o un metodo (seguono le stesse regole di typing):


$$
\frac{\Gamma [p_1 \rightarrow T_1] \ ... \ [p_n \rightarrow T_n] \vdash e : T}{\Gamma \vdash T \ foo(T_1 \ p_1, ..., T_n \ p_n) \ e;\
: \ (T_1, ..., T_n) \rightarrow T}[FunDef]
$$

$$
\frac{\Gamma \vdash foo : (T_1, ..., T_n) \rightarrow T \qquad a_1 : ST_1 <: T_1 \ \ldots \ a_n : ST_n <: T_n}{\Gamma \vdash foo(ST_1 \ a_1,...,ST_n \ a_n) : T}[FunApp]
$$



- `CLASSDEC` - una classe:


$$
\frac{\Gamma [a_1 \rightarrow T_1]...[a_n \rightarrow T_n][f_1p_1 \rightarrow F_1T_1]...[f_1p_n \rightarrow F_1T_n] \vdash e_1 : F_1 \ ... \ \Gamma [a_1 \rightarrow T_1]...[a_n \rightarrow T_n][f_np_1 \rightarrow F_nT_1] \ ... \ [f_np_n \rightarrow F_nT_n] \vdash e_n : F_n}{\Gamma \vdash class \ A \ (T_1 \ a_1, ..., T_n \ a_n) \ \{F_1 \ f_1(F_1T_1 \ f_1p_1, ..., F_1T_n \ f_1p_n) \ e_1; \ ... \ F_n \ f_n(F_nT_1 \ f_np_1, ..., F_nT_n \ f_np_n) \ e_n;\} : Class}[ClassDef]
$$



- `INSTANCE` - un'istanza di classe:



$$
\frac{
	\Gamma \vdash A : Class \qquad \Gamma \vdash A.a_1 : T_1 \ \ldots \ \Gamma \vdash A.a_n : T_n \qquad  a_1 : ST_1 <: T_1 \ \ldots \ a_n : ST_n <: T_n
}{
	\Gamma \vdash \text{new } A(ST_1 \ a_1, \ldots ,ST_n \ a_n) : Instance
}
[New]
$$

$$
\cfrac
{\raise{0.5ex}{\Gamma \vdash : Instance} \quad
	\raise{2.5ex}{\cfrac{}
	{\exists \ foo : (T_1,...,T_n) \rightarrow T \in
    methods(class(o))}[funApp]}}
{\Gamma \vdash o.foo(a_1,...,a_n) : T}[MethodCall]
$$



In aggiunta a queste regole, è stato anche definito l'operatore **let-in** per permettere l'introduzione di variabili all'interno di un'espressione:
$$
\frac{
\Gamma \vdash  e : T \quad
T <: T'' \quad
\Gamma[x \rightarrow T''] \vdash e' : T' \quad}
{\Gamma \vdash let \ T'' \ x \ = \ e \ in \ e' \ : T'}[LetIn]
$$

### 4.2 Subtyping

Si considerano in seguito le regole di subtyping per i tipi definiti precedentemente.

#### 4.2.1 Interi

$$
\frac{\Gamma \vdash n_1 : Int \quad \Gamma \vdash n_2 : Int}{\Gamma \vdash n_1 <: n_2}[IntSubtype]
$$

#### 4.2.2 Booleani

$$
\frac{\Gamma \vdash b_1 : Bool \quad \Gamma \vdash b_2 : Bool}{\Gamma \vdash b_1 <: b_2}[BoolSubtype]
$$

#### 4.2.1 Funzioni

La regola di subtyping per le funzioni è:


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
\cfrac
{
\raise{0.5ex}{\Gamma \vdash A : Class \quad \Gamma \vdash B:Class
\quad B \ \ implements \ \ A
\quad \forall \ a_i \in fields(A) ,\ \exists \ b_i \in fields(B) \ | \ b_i <: a_i
\quad \forall fb_i \in redefined\_methods(B) \ \exists \ fa_i \in methods(A) \ |}
\raise{2.5ex}{\cfrac{}{fb_i <: fa_i}[funSubtype]}
}
{
\Gamma \vdash B <: A
} [ClassDirectSubtype]
$$
$$
\cfrac
{
\raise{0.5ex}{\Gamma \vdash A : Class \quad \Gamma \vdash C : Class \qquad \exists \ B :Class \ \quad | \qquad}
\raise{2.5ex}{\cfrac{}{C <: B}[ClassDirectSubtype] \quad \cfrac{}{B <: A}[ClassDirectSubtype]}}
{\Gamma \vdash C <: A}[ClassIndirectSubtype]
$$

Quest'ultima regola di subtyping è implementata all'interno del file `ClassType.java`.

#### 4.2.3 Istanze

La regola di subtyping tra istanze di classi è:
$$
\cfrac{
\raise{0.5ex}{\Gamma \vdash a : Instance \quad \Gamma \vdash b : Instance \quad}
\raise{2.5ex}{\cfrac{}{class(a) <: class(b)}[classSubtype]}}{\Gamma \vdash a <: b}[InstanceSubtype]
$$
La regola `classSubtype` verifica sia l'eredetarietà diretta che quella indiretta seguendo le regole definite al paragrafo (4.2.2) precedente. Questa regola di subtyping è implementata nel file `InstanceType.java`.



## 5. Code generation

In questo capitolo affronteremo le parti più importanti della fase di generazione di codice riguardanti della nostra implementazione dell'estensione di FOOL ad oggetti.

### 5.1 Dispatch table

Le dispatch tables di tutti gli oggetti sono memorizzate in `CodegenUtils.java` nella seguente struttura dati che viene popolata durante la valutazione semantica:

```java
HashMap<String, ArrayList<DispatchTableEntry>> dispatchTables;
```

Attraverso il metodo `addDispatchTable`, ogni classe può aggiugere, usando come chiave il proprio nome, la sua dispatch table che altro non è che la lista **ordinata** dei suoi metodi. Infatti una `DispatchTableEntry` è costituta da:

| Nome           | Tipo     | Descrizione                              |
| -------------- | -------- | ---------------------------------------- |
| `method_id`    | `String` | Nome del metodo                          |
| `method_label` | `String` | Etichetta che segnala l'inizio del codice della funzione |

Infine, prima di concludere la fase di code generation, si concatena al codice `SVM` generato il risultato della chiamata al metodo `generateDispatchTablesCode()` che genera il codice relativo a tutte le dispatch tables aggiunte.  

### 5.2 Dichiarazione di classe

La code generation relativa ad una dichiarazione di classe (in `ClassNode.java`) altro non è che la costruzione corretta, rispetto anche alla superclasse, della dispatch table con questa modalità:

- Si crea una variabile `ArrayList<DispatchTableEntry> dispatchTable` che contenga la dispatch table della classe.
- Si assegna a `dispatchTable` la copia della dispatch table della superclasse, se esiste.
- Ciclando sui metodi di `dispatchTable` si controlla se esista un metodo con lo stesso nome in `metDecList`, ovvero nella classe in dichiarazione:
  - in caso positivo si sostituisce la `method_label` della classe padre con quella del metodo ridefinito.
- Infine, ciclando sui metodi di `metDecList` non ancora presenti nella dispatch table (ovvero quelli definiti solo nella classe corrente) si aggiunge una nuova `DispatchTableEntry` a `dispatchTable` 

### 5.3 Istanziazione di classe

La generazione di codice della creazione di un oggetto avviene in modo abbastanza semplice. Per prima cosa vengono pushati sullo stack tutti gli argomenti dati al costruttore. Successivamente viene pushato il numero di questi argomenti, il nome della classe ed infine il comando `new`.

### 5.4 Riferimento ad un campo

La generazione del codice per quanto riguarda l'accesso ad una variabile era giá implementata all'interno di `IdNode.java`, ciò che è stato aggiunto a questo file è l'accesso ad un campo della classe (concesso solo dall'interno di un metodo di essa). La distinzione fra i due casi precedenti avviene in base al valore booleano di ritorno del metodo `isAttribute()` chiamato  sull'oggetto `SymbolTableEntry` che è campo della classe `IdNode`.  

In caso si tratti di un campo di classe: si mette sullo stack per prima cosa l'offset della `SymbolTableEntry` ovvero l'offset del campo rispetto all'indirizzo di inizio della sua classe che viene pushato subito dopo. Successivamente si risale la catena statica dal *Frame Pointer* attuale per un totale di livelli (o *activation record*) uguale alla differenza di nesting level tra la referenza a `this` e la dichiarazione del campo. A questo punto la SVM può calcolare l'indirizzo finale del campo tramite due operazioni di `lw` e di `add` ed una di `hoff`.

### 5.5 Chiamata di un metodo

La generazione del codice per l'invocazione di un metodo è implementata all'interno del file `MethodCallNode.java` con queste modalità: 

- Si carica il valore di `$fp` sullo stack

- Per ogni `a` in `args` si inserisce sullo stack `codegen(a)`

- Si carica sullo stack l'`object_offset` e risale la catena statica fino a caricare l'indirizzo dell'activation record dove è definito l'oggetto

- Si sommano i due valori precedenti ottenendo e si carica sullo stack l'`object_address` ovvero l'indirizzo dell'oggetto nello heap

- SI carica sullo stack una copia dell'`object_address` e sommandola al `method_offset` si trova l'indirizzo di memoria nell'array `code` del metodo

- Tramite la nuova operazione di `lc` (descritta nel paragrafo 2.2.2) si pusha sullo stack l'indirizzo dell'array `code` a cui saltare 

- Infine si setta `$ra` = `$ip` e si salta alla prima istruzione del metodo impostando 

  `$ip` = `pop()`

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

Durante lo sviluppo è stato adottato un processo di Test Driven Development (**TDD**) in modo da evitare che con cambiamenti al codice sorgente si "rompessero" feature già funzionanti.

Nello specifico è stata creata una test suite dentro al file `test.yml`, il quale, adottando la sintassi YAML, presenta la seguente struttura:

```yaml
testId - descrizione del test:
-	codice fool
-	"risultato atteso"
```

Il file viene parsato e da ogni test viene estratto il codice fool, il quale viene eseguito e viene confrontato il risultato ottenuto con quello atteso. Se i due risultati sono diversi, il test viene segnato come fallito. Al termine dell'esecuzione di tutti i test viene indicato quanti di essi sono stati superati con successo.

Il codice originario non si prestava bene a questo tipo di procedimento, infatti non c'era modo né  di ottenere il risultato finale di un'esecuzione, né di ottenere un errore di type checking, in quanto erano inseriti dei `System.exit()` in caso di errori. È stato quindi fatto un refactoring del codice che ha inserito il sollevamento di una `TypeException` nel metodo `type`, ed è stato fatto in modo che il metodo `cpu` della SVM accumuli in `outputBuffer` i valori calcolati durante la computazione e restituisca alla fine di essa.
