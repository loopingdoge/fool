package exception;

import org.antlr.v4.runtime.ParserRuleContext;

public class TypeException extends Exception {

    public TypeException(String errorMessage, ParserRuleContext ctx) {
        super("Error: \"" + errorMessage + "\" at line " + ctx.start.getLine() + ", column " + ctx.start.getCharPositionInLine() + ".");
    }

}
