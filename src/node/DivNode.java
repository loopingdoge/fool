package node;

import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import type.IntType;
import type.Type;
import exception.TypeException;

import java.util.ArrayList;

public class DivNode extends Node {

    private INode left;
    private INode right;

    public DivNode(FOOLParser.TermContext ctx, INode left, INode right) {
        super(ctx);
        this.left = left;
        this.right = right;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        //create the result
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

        //check semantics in the left and in the right exp

        res.addAll(left.checkSemantics(env));
        res.addAll(right.checkSemantics(env));

        return res;
    }

    @Override
    public Type type() throws TypeException {
        if (!(left.type().isSubTypeOf(new IntType()) && right.type().isSubTypeOf(new IntType()))) {
            throw new TypeException("Non integers in division", ctx);
        }
        return new IntType();
    }

    @Override
    public String codeGeneration() {
        return left.codeGeneration() +
                right.codeGeneration() +
                "div\n";
    }

    @Override
    public ArrayList<INode> getChilds() {
        ArrayList<INode> childs = new ArrayList<>();

        childs.add(left);
        childs.add(right);

        return childs;
    }

    @Override
    public String toString() {
        return "Div";
    }

}  