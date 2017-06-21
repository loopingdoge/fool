package node;

import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import type.BoolType;
import type.IntType;
import type.Type;
import exception.TypeException;

import java.util.ArrayList;

public class TypeNode extends Node {

    Type type;

    public TypeNode(FOOLParser.TypeContext ctx, String type) {
        super(ctx);
        switch (type) {
            case "int":
                this.type = new IntType();
                break;
            case "bool":
                this.type = new BoolType();
                break;
        }
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        return new ArrayList<SemanticError>();
    }

    @Override
    public Type type() throws TypeException {
        return type;
    }

    @Override
    public String codeGeneration() {
        return "";
    }

    @Override
    public ArrayList<INode> getChilds() {
        return null;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
