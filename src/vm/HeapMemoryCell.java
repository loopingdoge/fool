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

    HeapMemoryCell(int index, HeapMemoryCell next) {
        this.index = index;
        this.next = next;
    }

    int getIndex() {
        return this.index;
    }

}
