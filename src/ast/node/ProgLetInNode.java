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
        env.nestingLevel++;
        HashMap<String, SymbolTableEntry> hm = new HashMap<String, SymbolTableEntry>();
        env.symTable.add(hm);

        //declare resulting list
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

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
        env.symTable.remove(env.nestingLevel--);

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
                FOOLlib.getCode();
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