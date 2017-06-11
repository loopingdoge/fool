package ast.node;

import ast.type.Type;
import ast.type.TypeException;
import parser.FOOLParser;
import util.Environment;
import util.SemanticError;

import java.util.ArrayList;

public class ThisNode extends Node {

    public ThisNode(FOOLParser.SingleExpContext ctx) {
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
