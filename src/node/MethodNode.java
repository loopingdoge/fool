package node;

import exception.RedeclaredVarException;
import exception.UndeclaredVarException;
import main.SemanticError;
import org.antlr.v4.runtime.ParserRuleContext;
import symbol_table.Environment;
import symbol_table.SymbolTableEntry;
import type.ClassType;
import type.FunType;
import type.InstanceType;
import type.Type;
import util.CodegenUtils;

import java.util.ArrayList;

public class MethodNode extends FunNode {

    private String classID;

    public MethodNode(ParserRuleContext ctx, String id, Type type, ArrayList<ParameterNode> params, ArrayList<INode> declarations, INode body) {
        super(ctx, id, type, params, declarations, body);
    }

    public void setClassID( String classID ) { this.classID = classID; }

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
            env.addEntry(this.id, new FunType(parTypes, declaredReturnType), env.offset--);
        } catch (RedeclaredVarException e) {
            res.add(new SemanticError("function " + id + " already declared"));
        }
        env.pushHashMap();

        try {
            SymbolTableEntry classEntry = env.getLatestEntryOfNotFun(classID);
            env.addEntry("this", new InstanceType((ClassType) classEntry.getType()), 0 );
        } catch (RedeclaredVarException | UndeclaredVarException e) {
            e.printStackTrace();
        }

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
        CodegenUtils.insertFunctionsCode(funl
                + ":\n"
                + "cfp\n"                   //setta $fp a $sp
                + "lra\n"                   //inserimento return address
                + declCode                  //inserimento dichiarazioni locali
                + body.codeGeneration()
                + "srv\n"                   //pop del return value
                + popDecl
                + "sra\n"                   // pop del return address
                + "pop\n"                   // pop di AL
                + popParl
                + "sfp\n"                   // setto $fp a valore del CL
                + "lrv\n"                   // risultato della funzione sullo stack
                + "lra\n"
                +"js\n"                      // salta a $ra
        );

        return funl + "\n";
    }

}