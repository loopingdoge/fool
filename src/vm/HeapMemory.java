package vm;

import exception.VMOutOfMemoryException;

class HeapMemory {

    /**
     * La prima cella di memoria libera
     */
    private HeapMemoryCell head;
    private int size;

    HeapMemory(int size) {
        HeapMemoryCell[] freelist = new HeapMemoryCell[size];
        this.size = size;
        // L'ultimo elemento della lista punta a null
        freelist[size - 1] = new HeapMemoryCell(ExecuteVM.MEMORY_START_ADDRESS + size - 1, null);

        // Tutti gli altri puntano al successivo
        for (int i = size - 2; i >= 0; i--) {
            freelist[i] = new HeapMemoryCell(ExecuteVM.MEMORY_START_ADDRESS + i, freelist[i + 1]);
        }

        // Il primo elemento e' la testa della lista
        head = freelist[0];
    }

    /**
     * Alloca un'area di memoria
     * @param size La dimensione della memoria da allocare
     * @return Una lista di celle di memoria
     */
    HeapMemoryCell allocate(int size) throws VMOutOfMemoryException {
        assert size > 0;

        if (this.size < size) throw new VMOutOfMemoryException(VMOutOfMemoryException.OverflowType.HEAP);

        // Il primo elemento da restituire e' la testa della lista
        HeapMemoryCell res = head;

        // La testa della lista diventa il primo elemento dopo l'ultimo restituito (alla fine del ciclo)
        HeapMemoryCell lastItem = head;
        for (int i = 1; i < size; i++) {
            lastItem = lastItem.next;
        }
        head = lastItem.next;

        // L'ultimo elemento restituito deve puntare a null
        lastItem.next = null;
        this.size -= size;
        return res;
    }

    /**
     * Dealloca la memoria passata come parametro, che torna ad essere disponibile come spazio di allocazione
     * @param firstCell La lista di celle di memoria
     */
    void deallocate(HeapMemoryCell firstCell) {
        int recoveredSize = 1;
        HeapMemoryCell curr = firstCell;

        // L'ultimo elemento della lista restituita va fatto puntare a head
        while (curr.next != null) {
            recoveredSize++;
            curr = curr.next;
        }
        curr.next = head;

        // La testa della freelist invece sara' il primo elemento della lista deallocata
        head = firstCell;
        this.size += recoveredSize;
    }

    int getNextFreeAddress() {
        if (head != null) {
            return head.getIndex();
        } else {
            return -1;
        }
    }

}
