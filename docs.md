# FOOL - Functional Object Oriented Language

#### Progetto del corso di Compilatori ed Interpreti AA 2016/2017

##### Corso di Laurea Magistrale in Informatica, Università di Bologna

##### ***Componenti del gruppo*** (in ordine alfabetico)

// [DEVID] metterei prima i nomi
- Pietro Battilana (matricola 799486)
- Mirco Civolani (matricola )
- Devid Farinelli (matricola 819683)
- Alberto Nicoletti (matricola 819697)

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

È stato necessario apportare modifiche anche alla *attribute grammar* dell'interprete FOOL. Per la operazioni di sottrazione e divisione sono stati semplicemente aggiungi i relativi terminali e non terminali `sub` e `div`. Invece per quanto riguarda l'operazione di '<=' è stata aggiunta una regola `BRANCHLESSQ` che si comporta in modo simile alla regola di '<' aggiungendo una *label* nel codice e alla collezione `labelRef` usata per fare *backpatching* alla fine della fase di parsing. È stata introdotta invece una nuova istruzione `LC` che come viene implementata come mostrato di seguito.

```java
case SVMParser.LC:
	int codeAddress = pop();
    push(code[codeAddress]);
	break;
```

come si può vedere svolge un operazione molto simile a `LOADW`, ovvero prende l'indirizzo in cima allo stack e con questo accede all'array `code`, infine carica sullo stack il valore ottenuto. Questa nuova istruzione è utilizzata nella chiamata ad un metodo ed il valore ottenuto da `LC` è la prima istruzione di tale metodo a cui si salta con l'operazione di `JS`.



Si è resa la dimensione dell'array `code`, contenente il bytecode, variabile a seconda del codice SVM prodotto dal compilatore FOOL. Ciò è stato fatto cambiando l'array `int[] code` nella sezione annotata come *@parser:members* in un private `ArrayList<Integer> code` di dimensioni inizialmente nulle.  Nelle regole per l'*assembly* per aggiungere un istruzione si chiama `code.add(instruction_int_code)`. In tal modo il codice sarà lungo esattamente quanto necessario senza sprechi di memoria. Si è modificato leggermente di conseguenza anche il *backpatching* per accedere ad ArrayList. 



## 3. Analisi semantica

## 4. Type checking

## 5. Code generation

## 6. Stack Vector Machine

## 7. Testing e conclusioni





### Attributi

La classe `C` dispone di:

| Attributo      | Tipo                | Descrizione                    |
| -------------- | ------------------- | ------------------------------ |
| `classID`      | `String`            | id della classe                |
| `superClassID` | `String`            | id della eventuale superclasse |
| `fields`       | `ArrayList<Field>`  | Lista dei tipi dei campi       |
| `methods`      | `ArrayList<Method>` | Lista dei tipi dei metodi      |

- Ogni `Field` è costituito da:

| Attributo | Tipo     | Descrizione         |
| --------- | -------- | ------------------- |
| `id`      | `String` | id dell'attributo   |
| `type`    | `Type`   | tipo dell'attributo |

- Ogni `Method` è costituito da:

| Attributo | Tipo      | Descrizione     |
| --------- | --------- | --------------- |
| `id`      | `String`  | id del metodo   |
| `type`    | `FunType` | tipo del metodo |

### Validazione semantica
- Creare una entry nella lista di symbol table
- Fare un push della symbol table (incrementando il nesting level)
- Per ogni attributo `f` in `fields`:
  - creare una entry nella symbol table per `f`
- Per ogni metodo `m` in `methods`:
  - creare una entry nella symbol table per `m`
- Se `C` estende un'altra classe `Super`:
  - recuperare dalla symbol table il classtype `supertype` di `Super`
  - scorrere `supertype.fields` con un indice `i = 0`
    - verificare che `supertype.fields[i].id` sia uguale a `fields[i].id`

### Validazione di tipo
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

### Code generation
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
