package exception;

public class UndeclaredClassException extends Exception {

    public UndeclaredClassException(String classID) { super("undeclared class: " + classID); }

}