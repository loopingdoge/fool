package node;

import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import type.IntType;
import type.Type;
import exception.TypeException;

import java.util.ArrayList;

public class IntNode extends Node {

    private int val;

    public IntNode(FOOLParser.IntValContext ctx, int val) {
        super(ctx);
        this.val = val;
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
    public String codeGeneration() {
        return "push " + val + "\n";
    }

    @Override
    public ArrayList<INode> getChilds() {
        return new ArrayList<>();
    }

    @Override
    public String toString(){
        return val + " : int";
    }

}  