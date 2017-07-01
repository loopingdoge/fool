package node;

import exception.TypeException;
import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import type.Type;
import util.CodegenUtils;

import java.util.ArrayList;

public class NotNode extends Node {

    private INode val;

    public NotNode(FOOLParser.BoolValContext ctx, INode val) {
        super(ctx);
        this.val = val;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        return new ArrayList<SemanticError>();
    }


    @Override
    public Type type() throws TypeException {
        return val.type();
    }

    @Override
    public String codeGeneration() {
        String l = CodegenUtils.freshLabel();
        String End = CodegenUtils.freshLabel();
        return val.codeGeneration() +
                "push 1\n" +
                "beq " + l + "\n" +
                "push 1\n" +
                "b " + End + "\n" +
                l + ":\n" +
                "push 0\n" +
                End + ":\n";
    }

    @Override
    public String toString() {
        return "Not ";
    }

    @Override
    public ArrayList<INode> getChilds() {
        ArrayList<INode> childs = new ArrayList<INode>();

        childs.add(val);

        return childs;
    }

}  