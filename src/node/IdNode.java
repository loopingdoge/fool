package node;

import exception.TypeException;
import exception.UndeclaredVarException;
import main.SemanticError;
import org.antlr.v4.runtime.ParserRuleContext;
import symbol_table.Environment;
import symbol_table.SymbolTableEntry;
import type.ArrowType;
import type.Type;

import java.util.ArrayList;

public class IdNode extends Node {

    private String id;
    private SymbolTableEntry entry;
    private int nestinglevel;

    public IdNode(ParserRuleContext ctx, String id) {
        super(ctx);
        this.id = id;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        //create result list
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

        try {
            this.entry = env.getLatestEntryOf(this.id);
            this.nestinglevel = env.getNestingLevel();
        } catch (UndeclaredVarException e) {
            res.add(new SemanticError(e.getMessage()));
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