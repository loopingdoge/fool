package node;

import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import type.IntType;
import type.Type;
import exception.TypeException;

import java.util.ArrayList;

public class MinusNode extends Node {

    private INode left;
    private INode right;

    public MinusNode(FOOLParser.ExpContext ctx, INode l, INode r) {
        super(ctx);
        left = l;
        right = r;
    }

    @Override
    public String codeGeneration() {
        return left.codeGeneration()
                + right.codeGeneration()
                + "sub\n";
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
            throw new TypeException("- allows only int type", ctx);
        }
        return new IntType();
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
        return "Minus";
    }

}
