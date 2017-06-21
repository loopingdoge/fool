package node;

import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import type.IntType;
import type.Type;
import type.TypeException;

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
        return new ArrayList<>();
    }

    public String codeGeneration() {
        return "push " + val + "\n";
    }

    @Override
    public String toString(){
        return val + " : int";
    }

}  