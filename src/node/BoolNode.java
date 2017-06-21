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
    private boolean not;

    public BoolNode(FOOLParser.BoolValContext ctx, boolean n, boolean no) {
        super(ctx);
        val = n;
        not = no;
    }

    @Override
    public Type type() throws TypeException {
        return new BoolType();
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        return new ArrayList<SemanticError>();
    }

    @Override
    public String codeGeneration() {
        if (not) {
            return "push " + (!val ? 1 : 0) + "\n";
        } else {
            return "push " + (val ? 1 : 0) + "\n";
        }
    }

    @Override
    public String toString(){
        return "Bool -> " + (not ? "not ": "") + val;
    }

    @Override
    public ArrayList<INode> getChilds() {
        return new ArrayList<>();
    }

}  