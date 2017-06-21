package node;

import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import type.Type;
import exception.TypeException;

import java.util.ArrayList;

public class PrintNode extends Node {

    private INode val;

    public PrintNode(FOOLParser.FunExpContext ctx, INode v) {
        super(ctx);
        val = v;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        return val.checkSemantics(env);
    }

    @Override
    public Type type() throws TypeException {
        return val.type();
    }

    @Override
    public String codeGeneration() {
        return val.codeGeneration() + "print\n";
    }

    @Override
    public ArrayList<INode> getChilds() {
        ArrayList<INode> childs = new ArrayList<>();

        childs.add(val);

        return childs;
    }

    @Override
    public String toString() {
        return "print";
    }

}  