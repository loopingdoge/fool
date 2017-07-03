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

Spiegare le modalità per importare e eseguire il progetto ... TODO

## 2. Analisi lessicale e sintattica

In questa sezione discuteremo delle grammatiche definite per il linguaggio FOOL e per il linguaggio SVM. In particolare ci soffermeremo sulle parti delle grammatiche modificate che riguardano le funzionalità aggiunte rispetto ai linguaggi forniti nella consegna.

### 2.1 Grammatica FOOL

Non è stato necessario modificare la produzione iniziale `prog` del linguaggio che può essere una semplice espressione oppure un espressione preceduta da dichiarazioni di variabili `let in` o di classi. Si è scelto di usare il non terminale `met`  per la definizione di metodi che per gestirli diversamente successivamente a livello semantico rispetto alla definizione di funzioni. Come è possibile vedere a riga 10, `met` è solo un wrapper per il non terminale `fun`. 

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

### 2.3 I nodi

#### 2.3.1 Considerazioni generali

Abbiamo scelto di lasciare l'interfaccia nodo come base per gli altri nodi, togliendo il metodo `toPrint()` (abbiamo utilizzato l'ovverriding della funzione nativa di Java `toString()` per stampare l'albero AST) ed aggiungendo un metodo `getChilds()` che restituisce un `<ArrayList>` di nodi coi figli del nodo interessato.

Il metodo `getChilds()` viene utilizzato in fase di stampa dell'albero AST.

#### 2.3.2 Nodi operatore

La grammatica inizialmente permetteva solamente l'utilizzo dell'operatore `==`, che rendeva il linguaggio davvero limitato. Abbiamo quindi scelto di sviluppare il punto opzionale aggiungendo `<=`, `>=`, `<`, `>` per quanto riguarda i confronti fra interi, `&&`, `||` invece per i confronti fra Booleani ed inoltre anche gli operatori di divisione, sottrazione e NOT.

Ogni nodo operatore (escluso NOT) presenta le stesse variabili (e parametri), ovvero:

- `INode left` : è l'elemento a sinistra dell'operazione;
- `INode right`: è l'elemento a destra dell'operazione.

Il nodo operatore NOT invece ha solamente un `INode` figlio che è ovviamente il booleano su cui si sta applicando il NOT.



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

Il type checking del codice preso in input dal compilatore viene eseguito dal basso verso l'alto (bottom-up) e viene chiamato ricorsivamente da ogni nodo padre a quello figlio. Ogni INode presenta un metodo `type()` che restituisce il tipo di quel nodo. Prima di restituire il tipo del nodo corrente, avviene un controllo di tipi sui figli, se presenti, e sui propri parametri e/o argomenti. Da questa logica si può capire facilmente come avvenga prima il controllo di tipi sui nodi figli e poi ricorsivamente si ritorna il tipo dell'intero AST.

Il tipo finale ritornato sarà il tipo dell'AST.

### 4.1 Classi Type

Durante il controllo dei tipi si va a controllare che, per un determinato nodo, operazione, funzione o classe, tutti i componenti rispettino le regole di tipaggio. Ogni struttura quindi ha le proprie regole di typing che sono diverse dalle altre, come ad esempio le regole di subtyping. Abbiamo scelto quindi di creare un'interfaccia Java *Type* su cui estendere il tipo di ogni nodo che riservi un 'trattamento' particolare. Nel nostro compilatore abbiamo i seguenti tipi:

- `VOIDTYPE` - nessun tipo.
- `BOOLTYPE` - un valore booleano;
- `INTTYPE` - un valore intero;
- `FUNTYPE` - una funzione, o metodo (seguono le stesse regole di typing per entrambi);
- `CLASSTYPE` - una classe;
- `INSTANCETYPE` - un'istanza di classe;

Ognuna di queste classi presenta i seguenti metodi ereditati dall'interfaccia:

- `String getID()` - restituisce il tipo `enum` (`BOOL`, `INT`, `FUN`, ecc);
- `boolean isSubtypeOf(Type t)` - restituisce vero se il tipo su cui viene richiamato questo metodo è sottotipo di `t`.

### 4.2 Subtyping

Il concetto di subtyping specifica come un tipo T sia sottotipo di S se è vera una delle seguenti affermazioni: T ed S sono lo stesso tipo; T è un'estensione di S.

Nell'implementazione del nostro compilatore l'analisi del subtyping diventa fondamentale con l'aggiunta delle classi (con relative estensioni), dei metodi e della loro sovrascrittura. Rimane fondamentale, ovviamente, anche per i tipi più semplici come booleani, interi o void, tuttavia in questi casi il nostro metodo *isSubtypeOf()* eseguirà solamente un confronto di uguaglianza fra i due tipi in questione (INT, BOOL e VOID non hanno estensioni di tipo nel nostro linguaggio FOOL).

#### 4.2.1 FunType

Come esplicitamente descritto nella consegna: *"Il tipo di una funzione f1 è sottotipo del tipo di una funzione f2 se il tipo ritornato da f1 è sottotipo del tipo ritornato da f2, se hanno il medesimo numero di parametri, e se ogni tipo di paramentro di f1 è sopratipo del corrisponde tipo di parametro di f2."* 

Questo è il nostro codice:

```java
public boolean isSubTypeOf(Type t) {
    if (t instanceof FunType) {
        FunType funType = (FunType) t;
        boolean check = true;

        if (this.params.size() == funType.getParams().size()) {
            for (int i = 0; i < this.params.size(); i++) {
                check &= funType.getParams().get(i).isSubTypeOf(this.params.get(i));
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



Abbiamo dunque, come prima cosa, controllato che il numero di parametri siano gli stessi, dopodiché, se il numero è lo stesso, controlliamo che ogni parametro di f1 sia sopratipo del corrispondente parametro di f2. Infine controlliamo che il tipo ritornato da f1 sia sottotipo del tipo ritornato da f2.

#### 4.2.2 ClassType

Sempre come riportato in consegna: *"Una classe C1 è sottotipo di una classe C2 se C1 estende C2 e se i campi e metodi che vengono sovrascritti sono sottotipi rispetto ai campi e metodi corrispondenti di C2. Inoltre, C1 è sottotipo di C2 se esiste una classe C3 sottotipo di C2 di cui C1 è sottotipo."*

Questo il codice:

```java
public boolean isSubTypeOf(Type t2) {
    if (t2 instanceof ClassType) {
        ClassType ct2 = (ClassType) t2;

      	if (this.getClassID().equals(ct2.getClassID())) {
            return true;
        }
      
        if (superType != null) {
            return this.getSuperclassID().equals(ct2.getClassID()) || superType.isSubTypeOf(t2);
        }
    }
    return false;
}
```

Qua per prima cosa verifichiamo se le due classi hanno lo stesso nome, in tal caso ritorniamo subito **true** senza fare ulteriori controlli. Se hanno nomi diversi allora controlliamo se sia una sottoclasse estesa di *t2*. Come si vede dal codice il valore booleano del return può essere true solo se:

- La classe corrente è una classe estesa di *t2*;
- La superclasse della classe corrente è sottotipo di *t2*.

Il secondo caso va a coprire quella casistica che vede un'estensione di classe che a sua volta era un'estensione di un'altra classe, in questo caso viene richiamando a sua volta il medesimo metodo, risalendo tutte le superclassi fino ad arrivare alla classe madre per verificarne se sia il suo sottotipo.

#### 4.2.3 InstanceType

Per quanto riguarda le istanze, il controllo di subtyping avviene controllando che l'istanza di classe su cui sto eseguendo il metodo sia sottotipo di un'altra istanza di classe, per confrontare due istanze non facciamo altro che richiamare il controllo di subtyping sulle relative classi di istanza:

```java
public boolean isSubTypeOf(Type type) {
    if (type instanceof InstanceType) {
        InstanceType it2 = (InstanceType) type;
        return classT.isSubTypeOf(it2.getClassType());
    } else {
        return false;
    }
}
```

### 4.3 Typecheck sugli operatori

Come detto nel paragrafo 2.3.2 ogni operatore ha due INode figli che rappresentano l'elemento di destra e l'elemento a sinistra dell'operatore. Nel caso di operatori che confrontano valori interi il nostro controllo di tipo andrà a verificare che il tipo del nodo di destra sia sottotipo del nodo di sinistra, e che siano quindi INT.

Nel caso invece di operatori booleani avverrà la medesima cosa ma accertandosi che entrambi siano sottotipi di BOOL.

## 5. Code generation

## 6. Stack Virtual Machine

Una volta generato il bytecode, questo viene eseguito da una **SVM** (Stack Virtual Machine), una macchina virtuale che dispone di memoria ed esegue il codice generato. La VM dispone di uno **stack**, che rappresenta la memoria della macchina, e la computazione e' espressa da ripetute modifiche allo stack tramite operazioni di **push** e **pop**.

La macchina virtuale richiede in input un parametro `int[] code`, un array contenente una serie di istruzioni definite nella grammatica `svm.g4`. Queste vengono lette una ad una e per ognuna di loro e' fornita una implementazione in Java in termini di operazioni di push e pop sullo stack.

Rispetto all'implementazione iniziale della VM, la modifica piu' degna di nota e' l'introduzione di una operazione di **new**, usata per allocare un oggetto in memoria. Gli oggetti infatti non possono vivere nello stack, e per questo motivo la loro creazione non puo' essere definita tramite operazioni di push e pop.

Per risolvere questo problema l'operazione new alloca gli oggetti nella parte piu' alta dello stack, in un'area denominata **heap**.

### 6.1 Heap

Lo heap e' implementato tramite una lista libera e rende disponibili i metodi:

- `HeapMemoryCell allocate(int size) throws VMOutOfMemoryException`
  - alloca un'area di memoria, rimuovendo dalla lista libera `size` elementi, e restituisce al chiamante il primo elemento rimosso. Da questo e' possibile accedere agli elementi successivi tramite il suo attributo `next`
  - nel caso la memoria richiesta sia superiore a quella disponibile, viene lanciata un'eccezione
- `void deallocate(HeapMemoryCell firstCell)`
  - dealloca la memoria il cui primo blocco viene passato come parametro e la reinserisce nella lista libera, in modo che torni ad essere disponibile per l'allocazione

E' stato scelto di implementare lo heap con una lista libera in modo da facilitare la gestione della **garbage collection**, infatti dopo una serie di allocazioni e deallocazioni di dimensioni differenti, lo heap potrebbe presentare *frammentazione interna*. L'uso della lista libera permette alla VM di ottenere blocchi di memoria logicamente ma non fisicamente contigui, in modo che essa possa operare senza tenere conto di questo problema.

### 6.2 Garbage Collection

E' stato realizzato un garbage collector usando la tecnica **mark and sweep**: se l'indirizzo di un oggetto non viene trovato nello stack o nel registro *RV*, allora tale oggetto puo' essere deallocato. Usando semplici numeri interi per rappresentare l'indirizzo di un oggetto, la ricerca di un indirizzo attivo sullo stack puo' produrre *falsi positivi*, poiche' l'intero trovato potrebbe non fare riferimento all'oggetto ma ad un semplice valore numerico. Per ridurre la probabilita' di ottenere falsi positivi e' stato introdotto un offset con un numero elevato per indicare l'inizio della memoria, in quanto ad esempio il numero 0 (che e' sempre presente come primo valore sullo stack) o in generale numeri bassi sono piu' facilmente trovabili in programmi comuni, si pensi ad un iteratore.

L'operazione di garbage collection viene eseguita se prima di allocare un oggetto, la differenza tra `sp` e `hp` e' minore o uguale al massimo tra:

- 5% della memoria totale
- 10 (in caso di memoria particolarmente piccola)

## 7. Testing e conclusioni

### Validazione di tipo (classdec)

- Per ogni `f` in `fields`:
  - eseguire il typecheck di `f`
- Per ogni `m` in `methods`:
  - per ogni dichiarazione `d` in `m`:
    - eseguire il typecheck di `d`
  - verificare che il tipo di `m.body` sia sottotipo di `m.returntype` (il tipo di ritorno dichiarato dal metodo)
- Se `C` estende un'altra classe `Super`:
  - recuperare dalla symbol table il classtype `supertype` di `Super`
  - recuperare la mappa dei metodi `super_methods_map` da `supertype`
  - per ogni metodo `m` in `methods`:
    - se `super_methods_map` contiene un `overridden_m` tale `overridden_m.id` sia uguale a `m.id`
      - verificare che `m` sia sottotipo di `overridden_m`
- Restituire `C.classtype`

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

### Code generation

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
