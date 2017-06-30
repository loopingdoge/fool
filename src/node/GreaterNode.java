package node;

import exception.TypeException;
import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import type.BoolType;
import type.IntType;
import type.Type;
import util.CodegenUtils;

import java.util.ArrayList;

public class GreaterNode extends Node {

    private INode left;
    private INode right;

    public GreaterNode(FOOLParser.FactorContext ctx, INode l, INode r) {
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
        if (!l.isSubTypeOf(new IntType()) || !r.isSubTypeOf(new IntType())) {
            throw new TypeException("Incompatible type for > (must be int)", ctx);
        }
        return new BoolType();
    }

    @Override
    public String codeGeneration() {
        String l1 = CodegenUtils.freshLabel();
        String l2 = CodegenUtils.freshLabel();
        //Dal Less Equal basta invertire l'ordine dei due valori nello stack iniziale su cui andrà a valutare il 'bleq'
        return right.codeGeneration() +
                "push 1\n" +
                "add\n" +
                left.codeGeneration() +
                "bleq " + l1 + "\n" +
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
        return ">";
    }

}  