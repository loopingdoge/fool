package node;

import org.antlr.v4.runtime.ParserRuleContext;
import type.Type;
import util.CodegenUtils;

import java.util.ArrayList;

public class MethodNode extends FunNode {

    private String classID;

    public MethodNode(ParserRuleContext ctx, String id, Type type, ArrayList<ParameterNode> params, ArrayList<INode> declarations, INode body) {
        super(ctx, id, type, params, declarations, body);
    }

    public void setClassID( String classID ) { this.classID = classID; }

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
                "pop\n" + // tolgo il puntatore all'oggetto dallo stack - [NUOVO] rispetto al codegen di FunNode TODO: controllare che funzioni con MethodCall
                "sfp\n" + // setto $fp a valore del CL
                "lrv\n" + // risultato della funzione sullo stack
                "lra\n" + "js\n" // salta a $ra
        );

        return funl + "\n";
    }

}