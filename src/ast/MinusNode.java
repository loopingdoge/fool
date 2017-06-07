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
        // TODO: implement
        return null;
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
