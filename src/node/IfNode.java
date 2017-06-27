package node;

import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import type.BoolType;
import type.Type;
import exception.TypeException;
import util.CodegenUtils;

import java.util.ArrayList;

public class IfNode extends Node {

    private INode cond;
    private INode th;
    private INode el;

    public IfNode(FOOLParser.IfExpContext ctx, INode cond, INode th, INode el) {
        super(ctx);
        this.cond = cond;
        this.th = th;
        this.el = el;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        //create the result
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

        //check semantics in the condition
        res.addAll(cond.checkSemantics(env));

        //check semantics in the then and in the else exp
        res.addAll(th.checkSemantics(env));
        res.addAll(el.checkSemantics(env));

        return res;
    }

    @Override
    public Type type() throws TypeException {
        if (!cond.type().isSubTypeOf(new BoolType())) {
            throw new TypeException("Non boolean condition in if", ctx);
        }
        Type t = th.type();
        Type e = el.type();
        if (t.isSubTypeOf(e)) {
            return e;
        } else if (e.isSubTypeOf(t)) {
            return t;
        } else {
            throw new TypeException("Incompatible types in then else branches", ctx);
        }
    }

    @Override
    public String codeGeneration() {
        String l1 = CodegenUtils.freshLabel();
        String l2 = CodegenUtils.freshLabel();
        return cond.codeGeneration() +
                "push 1\n" +
                "beq " + l1 + "\n" +
                el.codeGeneration() +
                "b " + l2 + "\n" +
                l1 + ":\n" +
                th.codeGeneration() +
                l2 + ":\n";
    }

    @Override
    public ArrayList<INode> getChilds() {
        ArrayList<INode> childs = new ArrayList<>();

        childs.add(cond);
        childs.add(th);
        childs.add(el);

        return childs;
    }

    @Override
    public String toString() {
        return "if";
    }

}  