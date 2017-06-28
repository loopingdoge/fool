package node;

import exception.RedeclaredVarException;
import exception.TypeException;
import exception.UndeclaredClassException;
import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import type.ClassType;
import type.InstanceType;
import type.Type;
import util.CodegenUtils;

import java.util.ArrayList;

public class VarNode extends Node {

    private String id;
    private Type declaredType;
    private INode assignedExpression;

    public VarNode(FOOLParser.VarasmContext ctx, String id, Type declaredType, INode assignedExpression) {
        super(ctx);
        this.id = id;
        this.declaredType = declaredType;
        this.assignedExpression = assignedExpression;
    }

    public Type getDeclaredType() {
        return this.declaredType;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        //create result list
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

        res.addAll(assignedExpression.checkSemantics(env));

        //Se sto istanziando un nuovo oggetto, aggiorno bene le informazioni di ClassType :=D
        if (assignedExpression instanceof NewNode) {
            NewNode newIstance = (NewNode) assignedExpression;
            try {
                ClassType CT = CodegenUtils.getClassEntry(newIstance.getClassID());
                this.declaredType = new InstanceType(CT);
            } catch (UndeclaredClassException e) {
                res.add(new SemanticError(e.getMessage()));
            }
        }

        //env.offset = -2;
        try {
            env.addEntry(id, this.declaredType, env.offset--);
        } catch (RedeclaredVarException e) {
            res.add(new SemanticError(e.getMessage()));
        }

        return res;
    }

    @Override
    public Type type() throws TypeException {
        FOOLParser.VarasmContext varasmContext = (FOOLParser.VarasmContext) ctx;
        if (!assignedExpression.type().isSubTypeOf(declaredType)) {
            throw new TypeException("incompatible value for variable " + id, varasmContext.exp());
        }

        return this.declaredType;
    }

    @Override
    public String codeGeneration() {
        return assignedExpression.codeGeneration();
    }

    @Override
    public ArrayList<INode> getChilds() {
        ArrayList<INode> childs = new ArrayList<>();
        childs.add(assignedExpression);
        return childs;
    }

    @Override
    public String toString(){
        return "var " + id + ": " + declaredType + " =";
    }

}  