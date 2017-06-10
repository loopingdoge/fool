package ast;

import util.Environment;
import util.SemanticError;

import java.util.ArrayList;

public class MethodNode implements Node {

    private String id;
    private ArrayList<Node> params = new ArrayList<Node>();
    private Type type;
    private SymbolTableEntry entry;
    private int nestingLevel;

    public MethodNode(String id, ArrayList<Node> params, SymbolTableEntry entry, int nestingLevel) {
        this.id = id;
        this.params = params;
        this.entry = entry;
        this.nestingLevel = nestingLevel;
    }

    public MethodNode(String id, ArrayList<Node> params) {
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
    public String toPrint(String indent) {
        String paramsToString = params.stream()
                .map(param -> param.toPrint(indent + "  "))
                .reduce("", String::concat);
        return indent + "Call method: " + id
                + " at nesting level " + nestingLevel + "\n"
                + entry.toPrint(indent + "  ")
                + paramsToString;
    }

    @Override
    public Type typeCheck() {
        return new CallNode(id, params, entry, nestingLevel).typeCheck();
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        return new CallNode(id, params, entry, nestingLevel).checkSemantics(env);
    }

    @Override
    public String codeGeneration() {
        return "Method node to be implemented yet";
    }

    @Override
    public ArrayList<Node> getChilds() {
        return null;
    }

}
