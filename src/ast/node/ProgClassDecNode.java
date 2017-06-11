package ast.node;

import ast.type.Type;
import ast.type.TypeException;
import parser.FOOLParser;
import util.Environment;
import util.SemanticError;

import java.util.ArrayList;

public class ProgClassDecNode extends Node {

    private ArrayList<INode> classDeclarations;
    private ArrayList<INode> letDeclarations;
    private INode exp;

    public ProgClassDecNode(FOOLParser.ClassExpContext ctx, ArrayList<INode> classDeclarations, ArrayList<INode> letDeclarations, INode exp) {
        super(ctx);
        this.classDeclarations = classDeclarations;
        this.letDeclarations = letDeclarations;
        this.exp = exp;
    }

    @Override
    public Type type() throws TypeException {
        return null; // not used?
    }

    @Override
    public String codeGeneration() {
        // TODO: implement
        return null;
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
        return res;
    }

    @Override
    public String toString() {
        return "class declarations";
    }

}
