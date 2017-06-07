package ast;

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
        // TODO: implement
        return null;
    }

    @Override
    public Node typeCheck() {
        // TODO: implement
        return null;
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
