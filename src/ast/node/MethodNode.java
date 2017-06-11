package ast.node;

import ast.SymbolTableEntry;
import ast.type.Type;
import ast.type.TypeException;
import parser.FOOLParser;
import util.Environment;
import util.SemanticError;

import java.util.ArrayList;

public class MethodNode extends Node {

    private String id;
    private ArrayList<INode> params = new ArrayList<INode>();
    private Type type;
    private SymbolTableEntry entry;
    private int nestingLevel;

    public MethodNode(FOOLParser.MethodExpContext ctx, String id, ArrayList<INode> params, SymbolTableEntry entry, int nestingLevel) {
        super(ctx);
        this.id = id;
        this.params = params;
        this.entry = entry;
        this.nestingLevel = nestingLevel;
    }

    public MethodNode(FOOLParser.MethodExpContext ctx, String id, ArrayList<INode> params) {
        super(ctx);
        this.id = id;
        this.params = params;
    }

    public String getId() {
        return id;
    }

    // TODO: implement method signature as its Type
    public Type getType() {
        return type;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        return null;
    }

    @Override
    public Type type() throws TypeException {
        return null;
    }

    @Override
    public String codeGeneration() {
        return "Method node to be implemented yet";
    }

    @Override
    public ArrayList<INode> getChilds() {
        return null;
    }

    @Override
    public String toString() {
        return id + "() " + "nl: " + nestingLevel + " " + entry.toPrint("");
    }

}
