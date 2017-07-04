package exception;

import java.util.ArrayList;

public class LexerException extends Throwable {

    ArrayList<String> errors;

    public LexerException(ArrayList<String> errors) {
        super(errors.stream().reduce("", (prev, curr) -> prev + "\n" + curr));
    }

}