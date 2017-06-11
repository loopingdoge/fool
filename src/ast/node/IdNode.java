package ast.node;

import ast.SymbolTableEntry;
import ast.type.ArrowType;
import ast.type.Type;
import ast.type.TypeException;
import parser.FOOLParser;
import util.Environment;
import util.SemanticError;

import java.util.ArrayList;

public class IdNode extends Node {

    private String id;
    private SymbolTableEntry entry;
    private int nestinglevel;

    public IdNode(FOOLParser.VarExpContext ctx, String i) {
        super(ctx);
        id = i;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        //create result list
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

        int j = env.nestingLevel;
        SymbolTableEntry tmp = null;
        while (j >= 0 && tmp == null)
            tmp = (env.symTable.get(j--)).get(id);
        if (tmp == null)
            res.add(new SemanticError("Id " + id + " not declared"));

        else {
            entry = tmp;
            nestinglevel = env.nestingLevel;
        }

        return res;
    }

    @Override
    public Type type() throws TypeException {
        if (entry.getType() instanceof ArrowType) {
            throw new TypeException("Wrong usage of function identifier", ctx);
        }
        return entry.getType();
    }

    public String codeGeneration() {
        StringBuilder getAR = new StringBuilder();
        for (int i = 0; i < nestinglevel - entry.getNestinglevel(); i++)
            getAR.append("lw\n");
        return "push " + entry.getOffset() + "\n" + //metto offset sullo stack
                "lfp\n" + getAR + //risalgo la catena statica
                "add\n" +
                "lw\n"; //carico sullo stack il valore all'indirizzo ottenuto

    }

    @Override
    public ArrayList<INode> getChilds() {
        return new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Id -> " + id;
    }

}  