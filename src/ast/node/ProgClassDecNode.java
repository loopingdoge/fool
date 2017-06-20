package ast.node;

import ast.type.Type;
import ast.type.TypeException;
import ast.type.VoidType;
import org.antlr.v4.runtime.ParserRuleContext;
import parser.FOOLParser;
import util.Environment;
import util.SemanticError;

import java.util.ArrayList;

public class ProgClassDecNode extends Node {

    private ArrayList<ClassNode> classDeclarations;
    private ArrayList<INode> letDeclarations;
    private INode exp;

    public ProgClassDecNode(ParserRuleContext ctx, ArrayList<ClassNode> classDeclarations, ArrayList<INode> letDeclarations, INode exp) {
        super(ctx);
        this.classDeclarations = classDeclarations;
        this.letDeclarations = letDeclarations;
        this.exp = exp;
    }

    @Override
    public Type type() throws TypeException {
        return new VoidType();
    }

    @Override
    public String codeGeneration() {
        // TODO: implement
        return "";
    }

    @Override
    public ArrayList<INode> getChilds() {
        ArrayList<INode> childs = new ArrayList<>();
        childs.addAll(classDeclarations);
        childs.addAll(letDeclarations);
        childs.add(exp);
        return childs;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();
        // TODO: implement

        for (ClassNode classNode : classDeclarations) {
            res.addAll(classNode.checkSemantics(env));
        }


        return res;
    }

    @Override
    public String toString() {
        return "class declarations";
    }

}
