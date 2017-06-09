package main;

import util.SemanticError;

import java.util.ArrayList;

class SemanticException extends Exception {

    private String semanticError;

    public SemanticException(ArrayList<SemanticError> errorsList) {
        super();
        StringBuilder errors = new StringBuilder();
        errors.append("You had: ")
                .append(errorsList.size())
                .append(" errors:");
        for (SemanticError e : errorsList) {
            errors.append("\t").
                    append(e);
        }
        this.semanticError = errors.toString();
    }

    @Override
    public String getMessage() {
        return this.semanticError;
    }
}
