package exception;

public class RedeclaredVarException extends Exception {

    public RedeclaredVarException(String id) {
        super("multiple definitions of variable: " + id);
    }

}
