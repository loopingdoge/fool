package exception;

public class UndeclaredVarException extends Exception {

    public UndeclaredVarException(String id) {
        super("undeclared variable " + id);
    }

}
