package node;

import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import symbol_table.SymbolTableEntry;
import symbol_table.UndeclaredVarException;
import type.ArrowType;
import type.Type;
import type.TypeException;
import type.TypeID;

import java.util.ArrayList;

public class CallNode extends Node {

    private String id;
    private ArrayList<INode> params = new ArrayList<INode>();
    private SymbolTableEntry entry;
    private int callNestingLevel;

    public CallNode(FOOLParser.FunExpContext ctx, String id, ArrayList<INode> params, SymbolTableEntry entry, int nestingLevel) {
        super(ctx);
        this.id = id;
        this.params = params;
        this.entry = entry;
        this.callNestingLevel = nestingLevel;
    }

    public CallNode(FOOLParser.FunExpContext ctx, String id, ArrayList<INode> params) {
        super(ctx);
        this.id = id;
        this.params = params;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        //create the result
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

        try {
            this.entry = env.getLatestEntryOf(id);
        } catch (UndeclaredVarException e) {
            res.add(new SemanticError("Id " + id + " not declared"));
        }

        this.callNestingLevel = env.getNestingLevel();

        for (INode arg : params) {
            res.addAll(arg.checkSemantics(env));
        }

        return res;
    }

    @Override
    public Type type() throws TypeException {
        ArrowType t;
        if (entry.getType().getID().equals(TypeID.ARROW)) {
            t = (ArrowType) entry.getType();
        } else {
            throw new TypeException("Invocation of a non-function " + id, ctx);
        }

        ArrayList<Type> p = t.getParams();
        if (!(p.size() == params.size())) {
            throw new TypeException("Wrong number of parameters in the invocation of " + id, ctx);
        }
        for (int i = 0; i < params.size(); i++)
            if (!params.get(i).type().isSubTypeOf(p.get(i))) {
                throw new TypeException("Wrong type for " + (i + 1) + "-th parameter in the invocation of " + id, ctx);
            }
        return t.getReturnType();
    }

    public String codeGeneration() {
        StringBuilder parCode = new StringBuilder();
        for (int i = params.size() - 1; i >= 0; i--)
            parCode.append(params.get(i).codeGeneration());

        StringBuilder getAR = new StringBuilder();
        for (int i = 0; i < callNestingLevel - entry.getNestinglevel(); i++)
            getAR.append("lw\n");

        return "lfp\n" + //CL
                parCode +
                "lfp\n" + getAR + //setto AL risalendo la catena statica
                // ora recupero l'indirizzo a cui saltare e lo metto sullo stack
                "push " + entry.getOffset() + "\n" + //metto offset sullo stack
                "lfp\n" + getAR + //risalgo la catena statica
                "add\n" +
                "lw\n" + //carico sullo stack il valore all'indirizzo ottenuto
                "js\n";
    }

    @Override
    public ArrayList<INode> getChilds() {
        ArrayList<INode> childs = new ArrayList<>();

        if(params != null && params.size()>0)
            childs.addAll(params);

        return childs;
    }

    @Override
    public String toString() {
        return "Call -> " + id;
    }

}  