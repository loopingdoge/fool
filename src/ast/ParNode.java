package ast;

import java.util.ArrayList;

import util.Environment;
import util.SemanticError;

public class ParNode implements Node {

    private String id;
    private Type type;

    public ParNode(String i, Type t) {
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

    public String toPrint(String s) {
        return s + "Par:" + id + "\n"
                + s + "  " + type + "\n";
    }

    //non utilizzato
    public Type typeCheck() {
        return null;
    }

    //non utilizzato
    public String codeGeneration() {
        return "";
    }

    @Override
    public String toString(){
        return "Par -> " + id + ": " + type;
    }

    @Override
    public ArrayList<Node> getChilds() {
        return null;
    }

}  