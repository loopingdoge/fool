package ast;

import lib.FOOLlib;
import util.Environment;
import util.SemanticError;

import java.util.ArrayList;

public class MinusNode implements Node {

    private Node left;
    private Node right;

    public MinusNode (Node l, Node r) {
        left=l;
        right=r;
    }

    @Override
    public String toPrint(String indent) {
        return indent+"Minus\n" + left.toPrint(indent+"  ")
                + right.toPrint(indent+"  ") ;
    }

    @Override
    public Type typeCheck() {
        if (! ( FOOLlib.isSubtype(left.typeCheck(),new IntType()) &&
                FOOLlib.isSubtype(right.typeCheck(),new IntType()) ) ) {
            System.out.println("Non integers in diff");
            System.exit(0);
        }
        return new IntType();
    }

    @Override
    public String codeGeneration() {
        return left.codeGeneration()
                + right.codeGeneration()
                + "sub\n";
    }

    @Override
    public String toString(){
        return "Minus";
    }

    @Override
    public ArrayList<Node> getChilds() {
        ArrayList<Node> childs = new ArrayList<>();

        childs.add(left);
        childs.add(right);

        return childs;
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
}
