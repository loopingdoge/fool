package ast.node;

import ast.type.BoolType;
import ast.type.Type;
import ast.type.TypeException;
import lib.FOOLlib;
import parser.FOOLParser;
import util.Environment;
import util.SemanticError;

import java.util.ArrayList;

public class OrNode extends Node {

    private INode left;
    private INode right;

    public OrNode(FOOLParser.FactorContext ctx, INode l, INode r) {
        super(ctx);
        left = l;
        right = r;
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
        Type l = left.type();
        Type r = right.type();
        if (!l.isSubTypeOf(new BoolType()) || !l.isSubTypeOf(new BoolType())) {
            throw new TypeException("|| allows only bool type", ctx);
        }
        return new BoolType();
    }

    public String codeGeneration() {
        String l1 = FOOLlib.freshLabel();
        String l2 = FOOLlib.freshLabel();
        return left.codeGeneration() +
                "push 1\n" +
                "beq " + l1 + "\n" +
                right.codeGeneration() +
                "push 1\n" +
                "beq " + l1 + "\n" +
                "push 0\n" +
                "b " + l2 + "\n" +
                l1 + ":\n" +
                "push 1\n" +
                l2 + ":\n";
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
        return "or";
    }

}  