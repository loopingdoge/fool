package vm;

class HeapMemoryCell {

    /**
     * La prossima cella di memoria libera
     */
    HeapMemoryCell next;
    /**
     * L'indice di memoria corrispondente a questa cella
     */
    private int index;
    private int referenceCounter;

    HeapMemoryCell(int index, HeapMemoryCell next) {
        this.index = index;
        this.next = next;
        this.referenceCounter = 0;
    }

    int getIndex() {
        return this.index;
    }

    int incReference() { return ++this.referenceCounter; }

    int decReference() { return --this.referenceCounter; }
}
