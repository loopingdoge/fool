package node;

import exception.TypeException;
import main.SemanticError;
import org.antlr.v4.runtime.ParserRuleContext;
import symbol_table.Environment;
import type.Type;
import type.VoidType;

import java.util.ArrayList;

public class ArgumentsNode extends Node {

    private ArrayList<INode> arguments;

    public ArgumentsNode(ParserRuleContext ctx, ArrayList<INode> arguments) {
        super(ctx);
        this.arguments = arguments;
    }

    public int size() {
        return arguments.size();
    }

    public INode get(int index) {
        return arguments.get(index);
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        ArrayList<SemanticError> res = new ArrayList<>();
        for (INode arg : arguments) {
            res.addAll(arg.checkSemantics(env));
        }
        return res;
    }

    @Override
    public Type type() throws TypeException {
        for (INode arg : arguments) {
            arg.type();
        }
        return new VoidType();
    }

    @Override
    public String codeGeneration() {
        StringBuilder code = new StringBuilder();
        for (INode arg : arguments) {
            code.append(arg.codeGeneration());
        }
        return code.toString();
    }

    @Override
    public ArrayList<INode> getChilds() {
        return arguments;
    }

    @Override
    public String toString() {
        return "args";
    }

}
