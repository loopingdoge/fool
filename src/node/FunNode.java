package node;

import exception.RedeclaredVarException;
import exception.TypeException;
import main.SemanticError;
import org.antlr.v4.runtime.ParserRuleContext;
import symbol_table.Environment;
import type.FunType;
import type.InstanceType;
import type.Type;
import util.CodegenUtils;

import java.util.ArrayList;

public class FunNode extends Node {

    protected String id;
    protected Type declaredReturnType;
    protected ArrayList<ParameterNode> params = new ArrayList<>();
    protected ArrayList<INode> declarations;
    protected INode body;

    public FunNode(ParserRuleContext ctx, String id, Type declaredReturnType, ArrayList<ParameterNode> params, ArrayList<INode> declarations, INode body) {
        super(ctx);
        this.id = id;
        this.declaredReturnType = declaredReturnType;
        this.params = params;
        this.declarations = declarations;
        this.body = body;
    }

    public String getId() {
        return this.id;
    }

    public ArrayList<ParameterNode> getParams() {
        return params;
    }

    public Type getDeclaredReturnType() {
        return this.declaredReturnType;
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
            // Se restituisco un oggetto, aggiorno le informazione sul ClassType
            if ( this.declaredReturnType instanceof InstanceType ) {
                InstanceType returnType = (InstanceType) this.declaredReturnType;
                res.addAll(returnType.updateClassType(env));
            }
            env.addEntry(this.id, new FunType(parTypes, declaredReturnType), env.offset--);
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

    @Override
    public Type type() throws TypeException {
        if (declarations != null) {
            for (INode dec : declarations) {
                dec.type();
            }
        }
        Type bodyType = body.type();
        if (!bodyType.isSubTypeOf(declaredReturnType)) {
            throw new TypeException("Incompatible return type in function " + id + " got '" + bodyType + "', must return '" + declaredReturnType + "'", ctx);
        }
        ArrayList<Type> paramsType = new ArrayList<>();
        for (ParameterNode param : params) {
            paramsType.add(param.getType());
        }

        return new FunType(paramsType, declaredReturnType);
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
        return declaredReturnType + " " + id + "()";
    }

}  