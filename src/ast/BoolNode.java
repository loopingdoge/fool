package ast;

import java.util.ArrayList;

import util.Environment;
import util.SemanticError;

public class BoolNode implements Node {

    private boolean val;
    private boolean not;

    public BoolNode(boolean n, boolean no) {
        val=n;
        not = no;
    }

    public String toPrint(String s) {
        if (not) return s + "Bool: not " + val + "\n";
        else return s + "Bool:" + val + "\n";
    }

    public Type typeCheck() {
        return new BoolType();
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {

        return new ArrayList<SemanticError>();
    }

    public String codeGeneration() {
        if(not){
            return "push " + (!val ? 1 : 0 ) + "\n";
        }else {
            return "push " + (val ? 1 : 0) + "\n";
        }
    }

    @Override
    public String toString(){
        return "Bool -> " + (not ? "not ": "") + val;
    }

    @Override
    public ArrayList<Node> getChilds() {
        return null;
    }

}  