package node;

import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import type.Type;
import exception.TypeException;
import util.CodegenUtils;

import java.util.ArrayList;

public class ProgLetInNode extends Node {

    private ArrayList<INode> declist;
    private INode exp;

    public ProgLetInNode(FOOLParser.LetInExpContext ctx, ArrayList<INode> d, INode e) {
        super(ctx);
        declist = d;
        exp = e;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

        env.pushHashMap();

        //check semantics in the dec list
        if (declist.size() > 0) {
            env.offset = -2;
            //if there are children then check semantics for every child and save the results
            for (INode n : declist)
                res.addAll(n.checkSemantics(env));
        }

        //check semantics in the exp body
        res.addAll(exp.checkSemantics(env));

        //clean the scope, we are leaving a let scope
        env.popHashMap();

        //return the result
        return res;
    }

    @Override
    public Type type() throws TypeException {
        for (INode dec : declist)
            dec.type();
        return exp.type();
    }

    public String codeGeneration() {
        StringBuilder declCode = new StringBuilder();
        for (INode dec : declist)
            declCode.append(dec.codeGeneration());
        return "push 0\n" +
                declCode +
                exp.codeGeneration() + "halt\n" +
                CodegenUtils.getFunctionsCode();
    }

    public ArrayList<INode> getChilds() {
        ArrayList<INode> childs = new ArrayList<>();

        childs.addAll(declist);
        childs.add(exp);

        return childs;
    }

    @Override
    public String toString() {
        return "let in";
    }

}  