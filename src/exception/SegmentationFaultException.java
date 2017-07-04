package exception;

public class SegmentationFaultException extends Throwable {

    public SegmentationFaultException() {
        super("Tried to access an invalid memory address, probably this is a compiler's error.");
    }

}
