package node;

import exception.TypeException;
import exception.UndeclaredVarException;
import main.SemanticError;
import org.antlr.v4.runtime.ParserRuleContext;
import symbol_table.Environment;
import symbol_table.SymbolTableEntry;
import type.InstanceType;
import type.Type;

import java.util.ArrayList;

public class ThisNode extends Node {

    private SymbolTableEntry entry;

    public ThisNode(ParserRuleContext ctx) {
        super(ctx);
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

        try {
            this.entry = env.getLatestEntryOf("this");
        } catch (UndeclaredVarException e) {
            res.add(new SemanticError(e.getMessage()));
        }

        return res;
    }

    @Override
    public Type type() throws TypeException {
        return entry.getType();
    }

    @Override
    public String codeGeneration() {
        return "push " + entry.getOffset() + "\n"
                + "lfp\n"
                + "add\n"
                + "lw\n";
    }

    @Override
    public ArrayList<INode> getChilds() {
        return new ArrayList<INode>();
    }

    @Override
    public String toString() {
        return "this";
    }
}
