package exception;

public class UndeclaredMethodException extends Exception {

    public UndeclaredMethodException(String methodID) {
        super("undeclared method: " + methodID);
    }

}
