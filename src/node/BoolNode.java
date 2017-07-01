package node;

import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import type.BoolType;
import type.Type;
import exception.TypeException;

import java.util.ArrayList;

public class BoolNode extends Node {

    private boolean val;

    public BoolNode(FOOLParser.BoolValContext ctx, boolean val) {
        super(ctx);
        this.val = val;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        return new ArrayList<SemanticError>();
    }


    @Override
    public Type type() throws TypeException {
        return new BoolType();
    }

    @Override
    public String codeGeneration() {
        return "push " + (val ? 1 : 0) + "\n";
    }

    @Override
    public String toString(){
        return "Bool -> " + val;
    }

    @Override
    public ArrayList<INode> getChilds() {
        return new ArrayList<>();
    }

}  