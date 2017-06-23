# Dispatch table
Contiene una hashmap:
- table : `String => ArrayList<String>`

Associa gli `id` delle classi ad una lista di `label` di funzioni.

Dispone dei metodi:
- `addClassEntry(String classID)`
- `addMethodInClass(String classID, String methodLabel)`

# Definizione di classe
La classe `C` dispone di:
- lista `fields` : `ArrayList<Field>`
  - `Field`
    - `id` : `String`
    - `type` : `Type`
- lista `methods` : `ArrayList<Method>`
  - `Method`
    - `id` : `String`
    - `type` : `FunType`

## Validazione semantica
- Creare una entry nella symbol table
- Fare un push sulla symbol table (incrementare il nesting level)
- Per ogni attributo `f` in `fields`:
  - creare una entry nella symbol table per `f`
- Per ogni metodo `m` in `methods`:
  - creare una entry nella symbol table per `m`
- Se `C` estende un'altra classe `Super`:
  - recuperare dalla symbol table il classtype `supertype` di `Super`
  - scorrere `supertype.fields` con un indice `i = 0`
    - verificare che `supertype.fields[i].id` sia uguale a `fields[i].id`

## Validazione di tipo
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

## Code generation
- Creare una lista `new_methods` che contiene `methods` meno `supertype.methods`
- Se `C` estende un'altra classe `Super`:
  - creare una nuova entry `c_entry` nella dispatch table copiando quella di `Super`
- Altrimenti:
  - creare una nuova entry `c_entry` vuota nella dispatch table
- Per ogni metodo `new_m` in `new_methods`:
  - creare una nuova label `new_m_label` per `new_m`
  - inserire `new_m_label` + `codegen(new_m)` nel codice delle funzioni
  - aggiungere in `c_entry` un nuovo metodo `new_m.id` con label `new_m_label`

# Istanziazione di classe (oggetto)
Istanziazione di un oggetto `obj`, dispone di:
- `classID` : `String`
- `args` : `ArgumentsNode`

## Validazione semantica
- Recuperare il `classtype` corrispondente a `classID` dalla tabella dei simboli
- Verificare che il numero di attributi passati al costruttore sia uguale a `classtype.fields.size()`

## Validazione di tipo
- Scorrere `args` con un indice `i = 0`:
  - verificare che `typecheck(args[i])` sia sottotipo di `typecheck(classtype.fields[i])`

## Code generation
- Allocare una nuova area di memoria nello heap
- Quando creo un oggetto di classe C, devo dargli un puntatore che punti alla dispatch table di classe C (TODO)
- TODO

# Chiamata di attributo
- TODO

## Validazione semantica
- TODO

## Validazione di tipo
- TODO

## Code generation
- TODO

# Chiamata di metodo
Si dispone di:
- `objectID` : `String`
- `methodID` : `String`
- `args` : `ArrayList<Node>`

## Validazione semantica
- Recuperare dalla symbol table l' `object_type` di `objectID`
- Verificare che `object_type` sia di tipo `InstanceType`
- Verificare che il `classtype` di `object_type` contenga un metodo che abbia `methodID` come `id` ed ottenerne il tipo `method_type`
- Verificare che il numero di `args` sia uguale al numero di parametri definito in `method_type`

## Validazione di tipo
- Scorrere `args` con un indice `i = 0`:
  - verificare che `typecheck(args[i])` sia sottotipo di `typecheck(method_type.params[i])`

## Code generation
- TODO

# Esempio

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

## Dispatch table

### Calculator

| Offset  | Value          |
| :------ | :------------- |
| 1       | label of xPlus |

### BetterCalculator

| Offset  | Value              |
| :------ | :----------------- |
| 1       | label of  xPlus    |
| 2       | label of  xPlusOne |

### WrongCalculator

| Offset  | Value                     |
| :------ | :------------------------ |
| 1       | label of overridden xPlus |

## Heap layout

### Calculator

| Row no.             | Value               |
| :------------------ | :------------------ |
| 1                   | value of x          |

### BetterCalculator

| Row no.             | Value               |
| :------------------ | :------------------ |
| 1                   | value of x          |

### WrongCalculator

| Row no.             | Value               |
| :------------------ | :------------------ |
| 1                   | value of x          |
| 2                   | value of y          |
