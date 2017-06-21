package node;

import main.SemanticError;
import org.antlr.v4.runtime.ParserRuleContext;
import symbol_table.Environment;
import symbol_table.RedeclaredVarException;
import type.ArrowType;
import type.Type;
import type.TypeException;
import util.CodegenUtils;

import java.util.ArrayList;

public class FunNode extends Node {

    private String id;
    private Type type;
    private ArrayList<ParameterNode> params = new ArrayList<>();
    private ArrayList<INode> declarations;
    private INode body;

    public FunNode(ParserRuleContext ctx, String id, Type type, ArrayList<ParameterNode> params, ArrayList<INode> declarations, INode body) {
        super(ctx);
        this.id = id;
        this.type = type;
        this.params = params;
        this.declarations = declarations;
        this.body = body;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        //create result list
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();
        ArrayList<Type> parTypes = new ArrayList<Type>();

        for (ParameterNode param : params) {
            parTypes.add(param.getType());
        }

        //env.offset = -2;
        try {
            env.addEntry(this.id, new ArrowType(parTypes, type), env.offset--);
            env.pushHashMap();

            int paroffset = 1;

            //check args
            for (ParameterNode param : params) {
                try {
                    env.addEntry(param.getId(), param.getType(), paroffset++);
                } catch (RedeclaredVarException e) {
                    res.add(new SemanticError(e.getMessage()));
                }
            }

            //check semantics in the dec list
            if (declarations.size() > 0) {
                env.offset = -2;
                //if there are children then check semantics for every child and save the results
                for (INode n : declarations)
                    res.addAll(n.checkSemantics(env));
            }

            //check body
            res.addAll(body.checkSemantics(env));

            //close scope
            env.popHashMap();
        } catch (RedeclaredVarException e) {
            res.add(new SemanticError("Fun id " + id + " already declared"));
        }


        return res;
    }

    @Override
    public Type type() throws TypeException {
        if (declarations != null)
            for (INode dec : declarations) {
                dec.type();
            }
        if (!body.type().isSubTypeOf(type)) {  // TODO: [Albi] Controllare che basti exp()
            throw new TypeException("Wrong return type for function: " + id, ctx);
        }
        // TODO: [Albi] secondo me anche le funzioni hanno un tipo
        return null;
    }

    public String codeGeneration() {

        StringBuilder declCode = new StringBuilder();
        if (declarations != null)
            for (INode dec : declarations)
                declCode.append(dec.codeGeneration());

        StringBuilder popDecl = new StringBuilder();
        if (declarations != null)
            for (INode dec : declarations)
                popDecl.append("pop\n");

        StringBuilder popParl = new StringBuilder();
        for (INode dec : params)
            popParl.append("pop\n");

        String funl = CodegenUtils.freshFunLabel();
        CodegenUtils.insertFunctionsCode(funl + ":\n" +
                "cfp\n" + //setta $fp a $sp
                "lra\n" + //inserimento return address
                declCode + //inserimento dichiarazioni locali
                body.codeGeneration() +
                "srv\n" + //pop del return value
                popDecl +
                "sra\n" + // pop del return address
                "pop\n" + // pop di AL
                popParl +
                "sfp\n" +  // setto $fp a valore del CL
                "lrv\n" + // risultato della funzione sullo stack
                "lra\n" + "js\n" // salta a $ra
        );

        return "push " + funl + "\n";
    }

    @Override
    public ArrayList<INode> getChilds() {
        ArrayList<INode> childs = new ArrayList<>();

        if (params != null && params.size() > 0) {
            childs.addAll(params);
        }

        if (declarations != null && declarations.size() > 0) {
            childs.addAll(declarations);
        }

        childs.add(body);

        return childs;
    }

    @Override
    public String toString() {
        return "Fun -> " + id + ": " + type;
    }

}  