package ast;

import lib.FOOLlib;
import util.Environment;
import util.SemanticError;

import java.util.ArrayList;

public class DivNode implements Node {

    private Node left;
    private Node right;

    public DivNode(Node l, Node r) {
        left=l;
        right=r;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        //create the result
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

        //check semantics in the left and in the right exp

        res.addAll(left.checkSemantics(env));
        res.addAll(right.checkSemantics(env));

        return res;
    }

    public String toPrint(String s) {
        return s+"Div\n" + left.toPrint(s+"  ")
                + right.toPrint(s+"  ") ;
    }

    public Type typeCheck() {
        if (! ( FOOLlib.isSubtype(left.typeCheck(),new IntType()) &&
                FOOLlib.isSubtype(right.typeCheck(),new IntType()) ) ) {
            System.out.println("Non integers in division");
            System.exit(0);
        }
        return new IntType();
    }

    public String codeGeneration() {
        return left.codeGeneration()+
                right.codeGeneration()+
                "div\n";
    }

    @Override
    public String toString(){
        return "Div";
    }

    @Override
    public ArrayList<Node> getChilds() {
        ArrayList<Node> childs = new ArrayList<>();

        childs.add(left);
        childs.add(right);

        return childs;
    }

}  