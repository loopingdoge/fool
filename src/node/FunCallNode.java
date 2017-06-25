package node;

import exception.TypeException;
import exception.UndeclaredVarException;
import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import symbol_table.SymbolTableEntry;
import type.FunType;
import type.Type;
import type.TypeID;

import java.util.ArrayList;

public class FunCallNode extends Node {

    protected String id;
    protected ArrayList<INode> params = new ArrayList<INode>();
    protected SymbolTableEntry entry = null;
    protected int callNestingLevel;

    public FunCallNode(FOOLParser.FuncallContext ctx, String id, ArrayList<INode> params, SymbolTableEntry entry, int nestingLevel) {
        super(ctx);
        this.id = id;
        this.params = params;
        this.entry = entry;
        this.callNestingLevel = nestingLevel;
    }

    public FunCallNode(FOOLParser.FuncallContext ctx, String id, ArrayList<INode> params) {
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
        FunType t;

        // TODO: [DEVID] questi controlli non dovrebbero essere in checksemantics?
        if (entry.getType().getID().equals(TypeID.FUN)) {
            t = (FunType) entry.getType();
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

    @Override
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

        if (params != null && params.size() > 0)
            childs.addAll(params);

        return childs;
    }

    @Override
    public String toString() {
        return "Call -> " + id;
    }
}
