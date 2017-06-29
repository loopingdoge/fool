package node;

import main.SemanticError;
import org.antlr.v4.runtime.ParserRuleContext;
import symbol_table.Environment;
import type.Type;
import exception.TypeException;

import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;

public class ProgClassDecNode extends Node {

    private ArrayList<ClassNode> classDeclarations;
    private LetNode let;
    private InNode in;

    public ProgClassDecNode(ParserRuleContext ctx, ArrayList<ClassNode> classDeclarations, LetNode l, InNode i) {
        super(ctx);
        this.classDeclarations = classDeclarations;
        this.let = l;
        this.in = i;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

        env.pushHashMap();

        Collections.sort(classDeclarations, new Comparator<ClassNode>() {
            @Override
            public int compare(ClassNode c1, ClassNode c2) {
                // -1: less than, 1: greater than, 0: equal, all inversed for descending
                int ret = 0;
                if(c1.getSuperClassID().equals(c2.getClassID()))// Se c1 estende c2
                    ret = 1;
                else if(c2.getSuperClassID().equals(c1.getClassID())) // Se c2 estende c1
                    ret = -1;
                return ret;
            }
        });

        for (ClassNode classNode : classDeclarations) {
            res.addAll(classNode.checkSemantics(env));
        }

        if (let != null)
            res.addAll(let.checkSemantics(env));

        res.addAll(in.checkSemantics(env));

        env.popHashMap();

        return res;
    }

    @Override
    public Type type() throws TypeException {
        for (ClassNode classdec : classDeclarations) {
            classdec.type();
        }
        if (let != null)
            let.type();

        return in.type();
    }

    @Override
    public String codeGeneration() {
        String declaration = "";
        for (ClassNode cl : classDeclarations) {
            declaration += cl.codeGeneration();
        }

        if (let != null)
            return declaration + let.codeGeneration() + in.codeGeneration();
        else
            return declaration + in.codeGeneration();
    }

    @Override
    public ArrayList<INode> getChilds() {
        ArrayList<INode> childs = new ArrayList<>();
        childs.addAll(classDeclarations);

        if (let != null)
            childs.add(let);

        childs.add(in);
        return childs;
    }

    @Override
    public String toString() {
        return "class declarations";
    }

}
