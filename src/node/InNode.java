package node;

import exception.TypeException;
import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import type.Type;
import util.CodegenUtils;

import java.util.ArrayList;

public class InNode extends Node {

    private INode exp;
    private boolean isLetIn;

    public InNode(FOOLParser.LetContext ctx, INode e, boolean f) {
        super(ctx);
        exp = e;
        isLetIn = f;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

        //check semantics in the exp body
        res.addAll(exp.checkSemantics(env));

        //return the result
        return res;
    }

    @Override
    public Type type() throws TypeException {
        return exp.type();
    }

    public String codeGeneration() {
        return exp.codeGeneration() + "halt\n" +
                CodegenUtils.getFunctionsCode();
    }

    public ArrayList<INode> getChilds() {
        ArrayList<INode> childs = new ArrayList<>();

        childs.add(exp);

        return childs;
    }

    @Override
    public String toString() {
        return isLetIn ? "in" : "exp";
    }

}