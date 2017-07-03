package node;

import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import type.Type;
import exception.TypeException;

import java.util.ArrayList;

public class ProgLetInNode extends Node {

    private LetNode let;
    private InNode in;

    public ProgLetInNode(FOOLParser.LetInExpContext ctx, LetNode let, InNode in) {
        super(ctx);
        this.let = let;
        this.in = in;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

        env.pushHashMap();

        //check semantics in the exp body
        res.addAll(let.checkSemantics(env));
        res.addAll(in.checkSemantics(env));

        //clean the scope, we are leaving a let scope
        env.popHashMap();

        //return the result
        return res;
    }

    @Override
    public Type type() throws TypeException {
        let.type();
        return in.type();
    }

    @Override
    public String codeGeneration() {
        return let.codeGeneration() + in.codeGeneration();
    }

    @Override
    public ArrayList<INode> getChilds() {
        ArrayList<INode> childs = new ArrayList<>();

        childs.add(let);
        childs.add(in);

        return childs;
    }

    @Override
    public String toString() {
        return "let in declaration";
    }

}  