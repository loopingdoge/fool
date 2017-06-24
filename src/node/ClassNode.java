package node;

import exception.RedeclaredClassException;
import exception.RedeclaredVarException;
import exception.TypeException;
import exception.UndeclaredVarException;
import main.SemanticError;
import org.antlr.v4.runtime.ParserRuleContext;
import symbol_table.Environment;
import symbol_table.SymbolTableEntry;
import type.ClassType;
import type.FunType;
import type.Type;
import util.CodegenUtils;
import util.Field;
import util.Method;

import java.util.ArrayList;
import java.util.HashMap;

public class ClassNode extends Node {


    private String classID;
    private String superClassID;
    private ArrayList<ParameterNode> vardeclist;
    private ArrayList<FunNode> fundeclist;

    private HashMap<String, Type> fields = new HashMap<>();
    private HashMap<String, FunType> methods = new HashMap<>();

    private ClassType type;

    public ClassNode(ParserRuleContext ctx, String classID, String superClassID, ArrayList<ParameterNode> vardeclist, ArrayList<FunNode> fundeclist) {
        super(ctx);
        this.classID = classID;
        this.superClassID = superClassID;
        this.vardeclist = vardeclist;
        this.fundeclist = fundeclist;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {

        ArrayList<SemanticError> res = new ArrayList<SemanticError>();
        ArrayList<Field> fieldsList = new ArrayList<>();
        ArrayList<Method> methodsList = new ArrayList<>();

        for (ParameterNode var : this.vardeclist) {
            fieldsList.add(new Field(var.getId(), var.getType()));
            fields.put(var.getId(), var.getType());
        }
        for (FunNode fun : fundeclist) {
            ArrayList<Type> paramsType = new ArrayList<>();
            for (ParameterNode param : fun.getParams()) {
                paramsType.add(param.getType());
            }

            methodsList.add(new Method(fun.getId(), new FunType(paramsType, fun.getType())));
            methods.put(fun.getId(), new FunType(paramsType, fun.getType()));
        }

        ClassType superclassType;
        try {
            superclassType = (ClassType) env.getLatestEntryOf(superClassID).getType();
        } catch (UndeclaredVarException e) {
            superclassType = null;
        }

        try {
            this.type = new ClassType(classID, superclassType, fieldsList, methodsList);
            env.addEntry(classID, this.type, 0);
            CodegenUtils.addClassEntry(classID, this.type);
        } catch (RedeclaredVarException | RedeclaredClassException ex) {
            res.add(new SemanticError(ex.getMessage()));
        }


        for (ParameterNode var : vardeclist) {
            res.addAll(var.checkSemantics(env));
        }
        for (FunNode fun : fundeclist) {
            res.addAll(fun.checkSemantics(env));
        }

        if (!superClassID.isEmpty()) {
            try {
                if (!(env.getTypeOf(superClassID) instanceof ClassType))
                    res.add(new SemanticError("ID of super class " + superClassID + " is not related to a class type"));
            } catch (UndeclaredVarException exp) {
                res.add(new SemanticError("Super class " + superClassID + " not defined"));
            }

            // TODO: accordarsi sul funzionamento dell'eredetarietà (spostare controlli in ClassType?)
            try {
                ClassType superClass = (ClassType) env.getLatestEntryOf(superClassID).getType();

                if (vardeclist.size() >= superClass.getFields().size()) {
                    for (int i = 0; i < superClass.getFields().size(); i++) {
                        if (!(superClass.getFields().get(i).getId().equals(vardeclist.get(i).getId()) && superClass.getFields().get(i).getType().getID().equals(vardeclist.get(i).getType().getID()))) {
                            res.add(new SemanticError("Subclass " + this.classID + " missing some superclass " + superClass.getClassID() + " parameters."));
                        }
                    }
                } else {
                    res.add(new SemanticError("Subclass has not the superclass parameters."));
                }
            } catch (UndeclaredVarException e) {
                res.add(new SemanticError("Super class " + superClassID + " not found in ST."));
            }

            try {
                SymbolTableEntry superClassEntry = env.getLatestEntryOf(superClassID);
                ClassType superClassType = (ClassType) superClassEntry.getType();

                HashMap<String, Type> superClassFields = superClassType.getFieldsMap();
                for (String localField : this.fields.keySet()) {
                    if (superClassFields.containsKey(localField)) {

                        if (!this.fields.get(localField).isSubTypeOf(superClassFields.get(localField))) {
                            res.add(new SemanticError("Field '" + localField + "'  overrided from super class with different type."));
                        }
                    }
                }

                HashMap<String, FunType> superClassMethods = superClassType.getMethodsMap();
                for (String localMethod : methods.keySet()) {
                    if (superClassMethods.containsKey(localMethod)) {
                        if (!methods.get(localMethod).isSubTypeOf(superClassMethods.get(localMethod))) {
                            res.add(new SemanticError("Method '" + localMethod + "'  overrided from super class with different type."));
                        }
                    }
                }

            } catch (UndeclaredVarException ex) {
                res.add(new SemanticError("Super class " + superClassID + " not defined. " + ex.getMessage()));
            }
        }

        return res;
    }

    @Override
    public Type type() throws TypeException {

        for (ParameterNode vardec : vardeclist){
            vardec.type();
        }

        for (FunNode fundec : fundeclist) {
            fundec.type();
        }

        return this.type;
    }

    public ArrayList<ParameterNode> getVardeclist() {
        return this.vardeclist;
    }

    @Override
    public String codeGeneration() {
        // TODO: implement
        return "";
    }

    @Override
    public ArrayList<INode> getChilds() {
        ArrayList<INode> res = new ArrayList<INode>();
        res.addAll(vardeclist);
        res.addAll(fundeclist);
        return res;
    }

    @Override
    public String toString() {
        if (!superClassID.isEmpty())
            return "class " + classID + " extends " + superClassID;
        else
            return "class " + classID;
    }

}
