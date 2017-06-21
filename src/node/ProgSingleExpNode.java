package node;

import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import type.Type;
import type.TypeException;

import java.util.ArrayList;

public class ProgSingleExpNode extends Node {

    private INode exp;

    public ProgSingleExpNode(FOOLParser.SingleExpContext ctx, INode e) {
        super(ctx);
        exp = e;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        return exp.checkSemantics(env);
    }

    @Override
    public Type type() throws TypeException {
        return exp.type();
    }

    @Override
    public String codeGeneration() {
        return exp.codeGeneration() + "halt\n";
    }

    @Override
    public ArrayList<INode> getChilds() {
        ArrayList<INode> childs = new ArrayList<>();
        childs.add(exp);
        return childs;
    }

    @Override
    public String toString() {
        return "single exp";
    }

}  