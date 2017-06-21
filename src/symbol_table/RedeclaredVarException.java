package symbol_table;

public class RedeclaredVarException extends Exception {

    public RedeclaredVarException(String id) {
        super("multiply declared variable " + id);
    }

}
