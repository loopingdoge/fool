package ast;

import java.util.ArrayList;

import util.Environment;
import util.SemanticError;

public class ProgNode implements Node {

    private Node exp;

    public ProgNode (Node e) {
        exp=e;
    }

    public String toPrint(String s) {

        return "Prog\n" + exp.toPrint("  ") ;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        return exp.checkSemantics(env);
    }

    public Type typeCheck() {
        return exp.typeCheck();
    }

    public String codeGeneration() {
        return exp.codeGeneration()+"halt\n";
    }

    @Override
    public String toString(){
        return "Prog";
    }

    @Override
    public ArrayList<Node> getChilds() {
        ArrayList<Node> childs = new ArrayList<>();

        childs.add(exp);

        return childs;
    }

}  