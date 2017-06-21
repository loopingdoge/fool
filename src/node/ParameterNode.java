package node;

import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import type.Type;
import exception.TypeException;

import java.util.ArrayList;

public class ParameterNode extends Node {

    private String id;
    private Type type;

    public ParameterNode(FOOLParser.VardecContext ctx, String i, Type t) {
        super(ctx);
        id = i;
        type = t;
    }

    public String getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        return new ArrayList<SemanticError>();
    }

    @Override
    public Type type() throws TypeException {
        return null;
    }

    public String toPrint(String s) {
        return s + "Par:" + id + "\n"
                + s + "  " + type + "\n";
    }

    //non utilizzato
    public String codeGeneration() {
        return "";
    }

    @Override
    public String toString(){
        return "param " + id + ": " + type;
    }

    @Override
    public ArrayList<INode> getChilds() {
        return new ArrayList<>();
    }

}  