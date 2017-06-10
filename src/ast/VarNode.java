package ast;

import java.util.ArrayList;
import java.util.HashMap;

import util.Environment;
import util.SemanticError;
import lib.FOOLlib;

public class VarNode implements Node {

    private String id;
    private Type type;
    private Node exp;

    public VarNode(String i, Type t, Node v) {
        id = i;
        type = t;
        exp = v;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        //create result list
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

        //env.offset = -2;
        HashMap<String, SymbolTableEntry> hm = env.symTable.get(env.nestingLevel);
        SymbolTableEntry entry = new SymbolTableEntry(env.nestingLevel, type, env.offset--); //separo introducendo "entry"

        if (hm.put(id, entry) != null)
            res.add(new SemanticError("Var id " + id + " already declared"));

        res.addAll(exp.checkSemantics(env));

        return res;
    }

    public String toPrint(String s) {
        return s + "Var:" + id + "\n"
                + s + " " + type + "\n"
                + exp.toPrint(s + "  ");
    }

    //valore di ritorno non utilizzato
    public Type typeCheck() {
        if (!(FOOLlib.isSubtype(exp.typeCheck(), type))) {
            System.out.println("incompatible value for variable " + id);
            System.exit(0);
        }
        return null;
    }

    public String codeGeneration() {
        return exp.codeGeneration();
    }

    @Override
    public ArrayList<Node> getChilds() {
        ArrayList<Node> childs = new ArrayList<>();

        childs.add(exp);

        return childs;
    }

    public Type getType() { return this.type; }

    public String getId() { return this.id; }

    @Override
    public String toString(){
        return "VarDec -> " + id + ": " + type;
    }

}  