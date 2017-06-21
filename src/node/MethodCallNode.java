package node;

import exception.TypeException;
import exception.UndeclaredVarException;
import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import symbol_table.SymbolTableEntry;
import type.*;

import java.util.ArrayList;

public class MethodCallNode extends Node {

    private String objectId;
    private String methodId;
    private ArrayList<ParameterNode> params = new ArrayList<>();
    private Type typeDeclaredInClass;

    public MethodCallNode(FOOLParser.MethodExpContext ctx, String objectId, String methodId, ArrayList<ParameterNode> params) {
        super(ctx);
        this.objectId = objectId;
        this.methodId = methodId;
        this.params = params;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        ArrayList<SemanticError> res = new ArrayList<>();

        try {
            // Se il metodo viene chiamato su this, vuol dire che stiamo facendo la semantic analysis
            // della classe, quindi prendo l'ultima entry aggiunta alla symbol table
            SymbolTableEntry classEntry = objectId.equals("this")
                    ? env.getLatestEntry()
                    : env.getLatestEntryOf(objectId);

            Type classType = new VoidType();
            if (objectId.equals("this")) {
                classType = env.getLatestEntry().getType();
            } else {
                Type objectType = env.getLatestEntryOf(objectId).getType();
                // Controllo che il metodo sia stato chiamato su un oggetto
                if (objectType instanceof InstanceType) {
                    classType = ((InstanceType) objectType).getClassType();
                } else {
                    res.add(new SemanticError("Method " + methodId + "called on a non-object type"));
                }
            }

            if (classType instanceof ClassType) {
                ClassType objectClass = (ClassType) classEntry.getType();
                // Controllo che il metodo esista all'interno della classe
                this.typeDeclaredInClass = objectClass.getMethods().get(methodId);
                if (this.typeDeclaredInClass == null) {
                    res.add(new SemanticError("Object " + objectId + " doesn't have a " + methodId + "method."));
                }
            }

        } catch (UndeclaredVarException e) {
            res.add(new SemanticError(e.getMessage()));
        }

        for (INode arg : params) {
            res.addAll(arg.checkSemantics(env));
        }

        return res;
    }

    @Override
    public Type type() throws TypeException {
        if (this.typeDeclaredInClass instanceof FunType) {
            FunType funType = (FunType) this.typeDeclaredInClass;
            ArrayList<Type> params = funType.getParams();
            if (!(params.size() == this.params.size())) {
                throw new TypeException("Wrong number of parameters in the invocation of " + methodId, ctx);
            }
            for (int i = 0; i < this.params.size(); i++) {
                if (!this.params.get(i).type().isSubTypeOf(params.get(i))) {
                    throw new TypeException("Wrong type for " + (i + 1) + "-th parameter in the invocation of " + methodId, ctx);
                }
            }
            return funType.getReturnType();
        } else {
            throw new TypeException("Invocation of a non-method " + methodId, ctx);
        }
    }

    @Override
    public String codeGeneration() {
        return "Method codegen not implemented yet";
    }

    @Override
    public ArrayList<INode> getChilds() {
        ArrayList<INode> childs = new ArrayList<>();
        childs.addAll(params);
        return childs;
    }

    @Override
    public String toString() {
        // TODO: add parameter names
        return objectId + "." + methodId + "()";
    }

}
