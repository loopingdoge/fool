package node;

import main.SemanticError;
import symbol_table.Environment;
import type.Type;
import type.TypeException;

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
