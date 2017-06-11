package ast.node;

import ast.SymbolTableEntry;
import ast.type.Type;
import ast.type.TypeException;
import lib.FOOLlib;
import parser.FOOLParser;
import util.Environment;
import util.SemanticError;

import java.util.ArrayList;
import java.util.HashMap;

public class VarNode extends Node {

    private String id;
    private Type type;
    private INode exp;

    public VarNode(FOOLParser.VarasmContext ctx, String i, Type t, INode v) {
        super(ctx);
        id = i;
        type = t;
        exp = v;
    }

    public Type getType() {
        return this.type;
    }

    public String getId() {
        return this.id;
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

    @Override
    public Type type() throws TypeException {
        FOOLParser.VarasmContext varasmContext = (FOOLParser.VarasmContext) ctx;
        if (!(FOOLlib.isSubtype(exp.type(), type))) {
            throw new TypeException("incompatible value for variable " + id, varasmContext.exp());
        }
        // TODO: [Albi] Controllare questo null
        return null;
    }

    @Override
    public String codeGeneration() {
        return exp.codeGeneration();
    }

    @Override
    public ArrayList<INode> getChilds() {
        ArrayList<INode> childs = new ArrayList<>();
        childs.add(exp);
        return childs;
    }

    @Override
    public String toString(){
        return id + ": " + type;
    }

}  