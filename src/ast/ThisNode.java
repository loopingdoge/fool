package ast;

import util.Environment;
import util.SemanticError;

import java.util.ArrayList;

public class ThisNode implements Node {
    @Override
    public String toPrint(String indent) {
        // TODO: implement
        return null;
    }

    @Override
    public Type typeCheck() {
        // TODO: implement
        return null;
    }

    @Override
    public String codeGeneration() {
        // TODO: implement
        return null;
    }

    @Override
    public ArrayList<Node> getChilds() {
        return null;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        // TODO: implement
        return null;
    }
}
