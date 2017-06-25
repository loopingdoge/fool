package node;

import exception.UndeclaredVarException;
import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import symbol_table.SymbolTableEntry;
import type.ClassType;
import type.InstanceType;
import type.Type;

import java.util.ArrayList;

public class MethodCallNode extends FunCallNode {

    private String objectId;
    private String methodId;
    private ArgumentsNode args;
    private Type methodType;

    public MethodCallNode(FOOLParser.MethodExpContext ctx, String objectId, String methodId, ArgumentsNode args) {
        super(ctx.funcall(), methodId, args.getChilds());
        this.objectId = objectId;
        this.methodId = methodId;
        this.args = args;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        ArrayList<SemanticError> res = new ArrayList<>();

        try {
            ClassType classType = null;
            if (objectId.equals("this")) {
                Type objectType = env.getLatestEntry().getType();
                if (objectType instanceof ClassType) {
                    classType = (ClassType) objectType;
                } else {
                    res.add(new SemanticError("Can't call this outside a class"));
                }
            } else {
                Type objectType = env.getLatestEntryOf(objectId).getType();
                // Controllo che il metodo sia stato chiamato su un oggetto
                if (objectType instanceof InstanceType) {
                    classType = ((InstanceType) objectType).getClassType();
                } else {
                    res.add(new SemanticError("Method " + methodId + " called on a non-object type"));
                }
            }

            // Se il metodo viene chiamato su this, vuol dire che stiamo facendo la semantic analysis
            // della classe, quindi prendo l'ultima entry aggiunta alla symbol table
            SymbolTableEntry classEntry = objectId.equals("this")
                    ? env.getLatestEntry()
                    : env.getLatestEntryOf(classType.getClassID());

            ClassType objectClass = (ClassType) classEntry.getType();
            // Controllo che il metodo esista all'interno della classe
            this.methodType = objectClass.getTypeOfMethod(methodId);
            if (this.methodType == null) {
                res.add(new SemanticError("Object " + objectId + " doesn't have a " + methodId + "method."));
            }

        } catch (UndeclaredVarException e) {
            res.add(new SemanticError(e.getMessage()));
        }

        args.checkSemantics(env);

        return res;
    }

    @Override
    public String codeGeneration() {
        StringBuilder parCode = new StringBuilder();
        for (int i = params.size() - 1; i >= 0; i--)
            parCode.append(params.get(i).codeGeneration());

        return "lfp\n" + //CL
                parCode +
                "push " + objectId +
                "lw\n" + //carico sullo stack il valore all'indirizzo ottenuto
                "js\n";
    }

    @Override
    public String toString() {
        return objectId + "." + methodId + "()";
    }

}
