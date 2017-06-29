
# FOOL - Functional Object Oriented Language

Setup
-
1) Aggiungere il file `antlr-4.7-complete.jar` come libreria
2) Aggiungere il file `snakeyaml-1.18.jar` come libreria
 
Descrizione:
-
PROGETTO COMPILATORI E INTERPRETI 2017

Il progetto consiste nella realizzazione di un compilatore per il linguaggio la cui sintassi è definita nel file FOOLPLUS.g. 

IL LINGUAGGIO
Questo linguaggio è una estensione object-oriented del linguaggio funzionale visto a 
lezione. In particolare 

* E' possibile dichiarare classi e sottoclassi. 
* Gli oggetti, che nascono come istanza di classi, contengono campi 
  (dichiarati nella classe o ereditati dalla super-classe) e metodi (esplicitamente 
  dichiarati nella classe o ereditati dalla super-classe). 
  Se in una sottoclasse viene dichiarato un campo o un metodo con il medesimo nome 
  di un campo della super-classe, tale campo o metodo sovrascrive quello della 
  super-classe. 
* I campi non sono modificabili ed il loro valore viene definito quando l'oggetto
  è creato.
* E' inoltre possibile dichiarare funzioni annidate. Le funzioni NON possono 
  essere passate come parametri.

IL TYPE-CHECKER
Il compilatore deve comprendere un type-checker che controlli il corretto uso dei tipi. Si deve 
considerare una nozione di subtyping fra classi e tipi di funzioni. 
* Il tipo di una funzione f1 è sottotipo del tipo di una funzione f2 se il tipo ritornato da f1 
  è sottotipo del tipo ritornato da f2, se hanno il medesimo numero di parametri, e se
  ogni tipo di paramentro di f1 è sopratipo del corrisponde tipo di parametro di f2. 
* Una classe C1 è sottotipo di una classe C2 se C1 estende C2 e se i campi e metodi che 
  vengono sovrascritti sono sottotipi rispetto ai campi e metodi corrispondenti di C2. 
  Inoltre, C1 è sottotipo di C2 se esiste una classe C3 sottotipo di C2 di cui C1 è 
  sottotipo.

IL CODICE OGGETTO
Il compilatore deve generare codice per un esecutore virtuale chiamato SVM (stack 
virtual machine) la cui sintassi è definita nel file SVM.g. Tale esecutore ha una 
memoria in cui gli indirizzi alti sono usati per uno stack. Uno stack pointer punta alla 
locazione successiva alla prossima locazione libera per lo stack (se la memoria ha 
indirizzi da 0 a MEMSIZE-1, lo stack pointer inizialmente punta a MEMSIZE). 
In questo modo, quando lo stack non è vuoto, lo stack pointer punta al top dello stack. 

Il programma è collocato in una memoria separata puntata dall' instruction pointer 
(che punta alla prossima istruzione da eseguire). Gli altri registri della macchina 
virtuale sono: HP (heap pointer), RA (return address), RV (return value) e FP 
(frame pointer). 
In particolare, HP serve per puntare alla prossima locazione disponibile dello 
heap; assumendo di usare gli indirizzi bassi per lo heap, HP contiene inizialmente 
il valore 0.

OPZIONALI
Le seguenti richieste non sono obbligatorie:
* Estensione del codice visto a lezione con operatori "<=", ">=", "||", "&&", "/", "-" 
   e "not"
* La deallocazione degli oggetti nello heap (garbage collection) NON è OBBLIGATORIA.
   Chi è interessato può scrivere il modulo relativo.