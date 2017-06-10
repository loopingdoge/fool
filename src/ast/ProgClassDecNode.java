package ast;

import util.Environment;
import util.SemanticError;

import java.util.ArrayList;
import java.util.HashMap;

public class ProgClassDecNode implements Node {

    ArrayList<Node> classDeclarations;
    ArrayList<Node> letDeclarations;
    Node exp;

    public ProgClassDecNode(ArrayList<Node> classDeclarations, ArrayList<Node> letDeclarations, Node exp) {
        this.classDeclarations = classDeclarations;
        this.letDeclarations = letDeclarations;
        this.exp = exp;
    }

    @Override
    public String toPrint(String indent) {

        StringBuilder strBuilder = new StringBuilder(indent + "ProgClassDecNode");

        for (Node classDec: classDeclarations)
            strBuilder.append(indent + classDec);

        for (Node letDec: letDeclarations)
            strBuilder.append(indent + letDec);

        strBuilder.append(indent + exp);

        return strBuilder.toString();
    }

    @Override
    public Type typeCheck() {

        return null; // not used?
    }

    @Override
    public String codeGeneration() {
        // TODO: implement
        return null;
    }

    @Override
    public ArrayList<Node> getChilds() {
        // TODO: implement
        return null;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();
        // TODO: implement
        return res;
    }
}
