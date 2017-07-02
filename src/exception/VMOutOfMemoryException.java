package exception;

public class VMOutOfMemoryException extends Throwable {

    public VMOutOfMemoryException(OverflowType type) {
        super(type == OverflowType.STACK ? "Stack overflow" : "Heap overflow");
    }

    public enum OverflowType {
        STACK,
        HEAP
    }

}
