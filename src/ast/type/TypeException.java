package ast.type;

import org.antlr.v4.runtime.ParserRuleContext;

public class TypeException extends Exception {

    private final String errorMessage;
    private final int line;
    private final int colStart;

    public TypeException(String errorMessage, ParserRuleContext ctx) {
        this.errorMessage = errorMessage;
        this.line = ctx.start.getLine();
        this.colStart = ctx.start.getCharPositionInLine();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getLine() {
        return line;
    }

    public int getColStart() {
        return colStart;
    }

    @Override
    public String toString() {
        return "Error: \"" + errorMessage + "\" at line " + line + ", column " + colStart + ".";
    }

}
