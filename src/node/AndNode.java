package node;

import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import type.BoolType;
import type.Type;
import exception.TypeException;
import util.CodegenUtils;

import java.util.ArrayList;

public class AndNode extends Node {

    private INode left;
    private INode right;

    public AndNode(FOOLParser.FactorContext ctx, INode left, INode right) {
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
        Type l = left.type();
        Type r = right.type();
        if (!(l.isSubTypeOf(r) || r.isSubTypeOf(l))) {
            throw new TypeException("Incompatible types in AND", ctx);
        }
        return new BoolType();
    }

    @Override
    public String codeGeneration() {
        String l1 = CodegenUtils.freshLabel();
        String l2 = CodegenUtils.freshLabel();
        String l3 = CodegenUtils.freshLabel();
        return  left.codeGeneration()
                + right.codeGeneration()
                + "beq " + l1 + "\n"
                + "push 0\n"
                + "b " + l2 + "\n"
                + l1 + ":\n"
                + left.codeGeneration()
                + "push 1\n"
                + "beq " + l3 + "\n"
                + "push 0\n"
                + "b " + l2 + "\n"
                + l3 + ":\n"
                + "push 1\n"
                + l2 + ":\n";
    }

    @Override
    public String toString() {
        return "And";
    }

    @Override
    public ArrayList<INode> getChilds() {
        ArrayList<INode> childs = new ArrayList<>();

        childs.add(left);
        childs.add(right);

        return childs;
    }

}  