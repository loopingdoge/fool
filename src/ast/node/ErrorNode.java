package ast.node;

import ast.type.Type;
import ast.type.TypeException;
import util.Environment;
import util.SemanticError;

import java.util.ArrayList;

public class ErrorNode implements INode {

    TypeException typeException;

    public ErrorNode(TypeException typeException) {
        this.typeException = typeException;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        return new ArrayList<SemanticError>();
    }

    @Override
    public Type type() throws TypeException {
        return null;
    }

    @Override
    public String codeGeneration() {
        return "";
    }

    @Override
    public ArrayList<INode> getChilds() {
        return new ArrayList<>();
    }

    @Override
    public String toString() {
        return typeException.toString();
    }
}
