package node;

import exception.TypeException;
import exception.UndeclaredClassException;
import exception.UndeclaredVarException;
import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import type.ClassType;
import type.InstanceType;
import type.Type;
import util.CodegenUtils;
import util.Field;

import java.util.ArrayList;

public class NewNode extends Node {

    private String classID;
    private ClassType classType;
    private ArgumentsNode args;

    public NewNode(FOOLParser.NewExpContext ctx, String classID, ArgumentsNode args) {
        super(ctx);
        this.classID = classID;
        this.args = args;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        //create the result
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

        try {

            try {
                //Catturo la classe all'interno dell'env poichè potrebbe usare classi non ancora dichiarate e quindi non presenti nella ST
                //Rigenero l'errore giusto poichè se non esiste la classe, il getTypeOf genera un UndeclaredVarException e non una UndeclaredClassException
                classType = (ClassType) env.getTypeOf(classID);
            } catch (Exception e1) {
                throw new UndeclaredClassException(classID);
            }

            if (classType.getFields().size() != args.size())
                res.add(new SemanticError("Instantiation of new " + classID + " with the wrong number of parameters."));

            //if there are children then check semantics for every child and save the results
            if (args.size() > 0)
                res.addAll(args.checkSemantics(env));

            env.getLatestEntryOf(this.classID);
        } catch (UndeclaredClassException | UndeclaredVarException e) {
            res.add(new SemanticError(e.getMessage()));
        }
        return res;
    }

    @Override
    public Type type() throws TypeException {
        ArrayList<Field> classFields = classType.getFields();
        for (int i = 0; i < args.size(); i++) {
            Type currentArgType = args.get(i).type();
            Type requestedType = classFields.get(i).getType();
            if (!currentArgType.isSubTypeOf(requestedType)) {
                throw new TypeException("Wrong type for " + (i + 1) + "-th parameter in the invocation of " + classID + " constructor", ctx);
            }
        }
        return new InstanceType(classType);
    }

    @Override
    public String codeGeneration() {
        StringBuilder argsCode = new StringBuilder();
        for (INode arg : args.getChilds()) {
            argsCode.append(arg.codeGeneration());
        }
        return argsCode
                + "push " + args.size() + "\n"
                + CodegenUtils.getDispatchTablePointer(classID) + "\n"
                + "new\n";
    }

    @Override
    public ArrayList<INode> getChilds() {
        ArrayList<INode> res = new ArrayList<>();

        res.addAll(args.getChilds());

        return res;
    }

    @Override
    public String toString() {
        return "new " + classID;
    }

}
