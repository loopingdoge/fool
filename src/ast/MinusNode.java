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
    public Node typeCheck() {
        if (! ( FOOLlib.isSubtype(left.typeCheck(),new IntTypeNode()) &&
                FOOLlib.isSubtype(right.typeCheck(),new IntTypeNode()) ) ) {
            System.out.println("Non integers in diff");
            System.exit(0);
        }
        return new IntTypeNode();
    }

    @Override
    public String codeGeneration() {
        return left.codeGeneration()
                + right.codeGeneration()
                + "sub\n";
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        // TODO: implement
        return null;
    }
}
