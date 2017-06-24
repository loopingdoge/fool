package node;

import exception.RedeclaredVarException;
import exception.TypeException;
import main.SemanticError;
import org.antlr.v4.runtime.ParserRuleContext;
import symbol_table.Environment;
import type.FunType;
import type.Type;
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

    public String getId() {
        return this.id;
    }

    public Type getType() {
        return this.type;
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

        //TODO: Deve per forza avere un parametro di ritorno definito insieme alla funzione? Nel dubbio ho fatto il controllo, controllare che il parametro di ritorno, se dichiarato ID(classe) esista.
        if (this.id.equals("<missing ID>")) {
            res.add(new SemanticError("Missing ID or Type in a function."));
        }
        try {
            env.addEntry(this.id, new FunType(parTypes, type), env.offset--);
        } catch (RedeclaredVarException e) {
            res.add(new SemanticError("function " + id + " already declared"));
        }
        env.pushHashMap();

        //check args
        for (ParameterNode param : params) {
            res.addAll(param.checkSemantics(env));
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


        return res;
    }

    public ArrayList<ParameterNode> getParams() {
        return params;
    }

    @Override
    public Type type() throws TypeException {
        if (declarations != null) {
            for (INode dec : declarations) {
                dec.type();
            }
        }
        if (!body.type().isSubTypeOf(type)) {  // TODO: [Albi] Controllare che basti exp()
            throw new TypeException("Wrong return type for function: " + id, ctx);
        }
        // FATTO: [Albi] secondo me anche le funzioni hanno un tipo
        ArrayList<Type> paramsType = new ArrayList<>();
        for (ParameterNode param : params) {
            paramsType.add(param.getType());
        }
        return new FunType(paramsType, type).getReturnType();
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