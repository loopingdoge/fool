package main;

import util.SemanticError;

import java.util.ArrayList;

public class ScopeException extends Exception {

    private String semanticError;

    public ScopeException(String message) {
        super(message);
        this.semanticError = message;
    }

    public ScopeException(ArrayList<SemanticError> errorsList) {
        super();
        StringBuilder errors = new StringBuilder();
        for (SemanticError e : errorsList) {
            errors.append(e).append("\n");
        }
        this.semanticError = errors.toString();
    }

    @Override
    public String getMessage() {
        return this.semanticError;
    }
}
