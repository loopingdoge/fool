package ast.node;

import ast.type.IntType;
import ast.type.Type;
import ast.type.TypeException;
import parser.FOOLParser;
import util.Environment;
import util.SemanticError;

import java.util.ArrayList;

public class IntNode extends Node {

    private Integer val;

    public IntNode(FOOLParser.IntValContext ctx, Integer n) {
        super(ctx);
        val = n;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        return new ArrayList<SemanticError>();
    }

    @Override
    public Type type() throws TypeException {
        return new IntType();
    }

    @Override
    public ArrayList<INode> getChilds() {
        return null;
    }

    public String codeGeneration() {
        return "push " + val + "\n";
    }

    @Override
    public String toString(){
        return val + " : int";
    }

}  