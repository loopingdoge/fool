package exception;

public class RedeclaredClassException extends Exception {

    public RedeclaredClassException(String classID) { super("multiple definitions of class: " + classID); }

}