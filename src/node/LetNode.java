package node;

import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import type.Type;
import exception.TypeException;
import util.CodegenUtils;

import java.util.ArrayList;

public class LetNode extends Node {

    private ArrayList<INode> declist;

    public LetNode(FOOLParser.LetContext ctx, ArrayList<INode> declist) {
        super(ctx);
        this.declist = declist;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

        //check semantics in the dec list
        if (declist.size() > 0) {
            env.offset = -2;
            //if there are children then check semantics for every child and save the results
            for (INode n : declist)
                res.addAll(n.checkSemantics(env));
        }

        //return the result
        return res;
    }

    @Override
    public Type type() throws TypeException {
        for (INode dec : declist)
            dec.type();
        return null;
    }

    public String codeGeneration() {
        StringBuilder declCode = new StringBuilder();
        for (INode dec : declist)
            declCode.append(dec.codeGeneration());
        return "push 0\n" +
                declCode;
    }

    public ArrayList<INode> getChilds() {
        ArrayList<INode> childs = new ArrayList<>();

        childs.addAll(declist);

        return childs;
    }

    @Override
    public String toString() {
        return "let";
    }

}