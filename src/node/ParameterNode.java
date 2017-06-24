package node;

import exception.RedeclaredVarException;
import exception.TypeException;
import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import type.Type;

import java.util.ArrayList;

public class ParameterNode extends Node {

    private String id;
    private Type type;
    private int offset;

    public ParameterNode(FOOLParser.VardecContext ctx, String id, Type type, int offset) {
        super(ctx);
        this.id = id;
        this.type = type;
        this.offset = offset;
    }

    public String getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        ArrayList<SemanticError> errors = new ArrayList<>();
        try {
            env.addEntry(id, type, offset);
        } catch (RedeclaredVarException e) {
            errors.add(new SemanticError(e.getMessage()));
        }
        return errors;
    }

    @Override
    public Type type() throws TypeException {
        return null;
    }

    public String codeGeneration() {
        return "";
    }

    @Override
    public String toString() {
        return "param " + id + ": " + type;
    }

    @Override
    public ArrayList<INode> getChilds() {
        return new ArrayList<>();
    }

}