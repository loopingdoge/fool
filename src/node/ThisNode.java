package node;

import exception.TypeException;
import main.SemanticError;
import org.antlr.v4.runtime.ParserRuleContext;
import symbol_table.Environment;
import type.Type;

import java.util.ArrayList;

public class ThisNode extends Node {

    public ThisNode(ParserRuleContext ctx) {
        super(ctx);
    }

    @Override
    public String codeGeneration() {
        // TODO: implement
        return null;
    }

    @Override
    public ArrayList<INode> getChilds() {
        return null;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        // TODO: implement
        return null;
    }

    @Override
    public Type type() throws TypeException {
        return null;
    }

    @Override
    public String toString() {
        return "this";
    }
}