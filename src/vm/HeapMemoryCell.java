package vm;

public class HeapMemoryCell {

    public int value;
    public HeapMemoryCell next;

    public HeapMemoryCell(int value, HeapMemoryCell next) {
        this.value = value;
        this.next = next;
    }

}
