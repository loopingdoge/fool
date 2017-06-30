package node;

import exception.TypeException;
import exception.UndeclaredVarException;
import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import type.*;

import java.util.ArrayList;

public class TypeNode extends Node {

    String declaredType;
    Type type;

    public TypeNode(FOOLParser.TypeContext ctx, String type) {
        super(ctx);
        declaredType = type;
        switch (type) {
            case "int":
                this.type = new IntType();
                break;
            case "bool":
                this.type = new BoolType();
                break;
            default:
                // TODO questo non setta superType, controllare che vada bene con l'ereditarieta'
                this.type = new InstanceType(type);
                break;
        }
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        ArrayList<SemanticError> res = new ArrayList<>();
        try {
            this.type = env.getLatestEntryOf(declaredType).getType();
        } catch (UndeclaredVarException e) {
            res.add(new SemanticError("Class '" + declaredType + "' does not exist"));
        }
        return res;
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
