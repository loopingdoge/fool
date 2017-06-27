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
import type.InstanceType;
import type.Type;
import util.CodegenUtils;
import util.DispatchTableEntry;
import util.Field;
import util.Method;

import java.util.ArrayList;
import java.util.HashMap;

public class ClassNode extends Node {


    private String classID;
    private String superClassID;
    private ArrayList<ParameterNode> vardeclist;
    private ArrayList<MethodNode> metdeclist;

    private HashMap<String, Type> fields = new HashMap<>();
    private HashMap<String, FunType> methods = new HashMap<>();

    private ClassType type;

    public ClassNode(ParserRuleContext ctx, String classID, String superClassID, ArrayList<ParameterNode> vardeclist, ArrayList<MethodNode> metdeclist) {
        super(ctx);
        this.classID = classID;
        this.superClassID = superClassID;
        this.vardeclist = vardeclist;
        this.metdeclist = metdeclist;
    }

    public ArrayList<ParameterNode> getVardeclist() {
        return this.vardeclist;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {

        ArrayList<SemanticError> res = new ArrayList<>();
        ArrayList<Field> fieldsList = new ArrayList<>();
        ArrayList<Method> methodsList = new ArrayList<>();

        for (ParameterNode var : this.vardeclist) {
            fieldsList.add(new Field(var.getId(), var.getType()));
            fields.put(var.getId(), var.getType());
        }
        for (MethodNode fun : metdeclist) {
            ArrayList<Type> paramsType = new ArrayList<>();
            for (ParameterNode param : fun.getParams()) {
                paramsType.add(param.getType());
            }

            methodsList.add(new Method(fun.getId(), new FunType(paramsType, fun.getDeclaredReturnType())));
            methods.put(fun.getId(), new FunType(paramsType, fun.getDeclaredReturnType()));
        }

        ClassType superclassType = null;
        try {
            superclassType = (ClassType) env.getLatestEntryOf(superClassID).getType();
        } catch (UndeclaredVarException e) {
            superclassType = null;
        }

        // Creo una entry nella symbol table
        try {
            this.type = new ClassType(classID, superclassType, fieldsList, methodsList);
            env.addEntry(classID, this.type, 0);
            CodegenUtils.addClassEntry(classID, this.type);
        } catch (RedeclaredVarException | RedeclaredClassException e) {
            res.add(new SemanticError(e.getMessage()));
        }

        env.pushHashMap();
        for (ParameterNode var : vardeclist) {
            res.addAll(var.checkSemantics(env));
        }
        try {
            env.addEntry("this", new InstanceType(type), vardeclist.size() + 1 );
        } catch (RedeclaredVarException e) {
            e.printStackTrace();
        }

        env.pushHashMap();
        for (MethodNode fun : metdeclist) {
            res.addAll(fun.checkSemantics(env));
        }
        env.popHashMap().popHashMap();

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

        for (MethodNode fundec : metdeclist) {
            fundec.type();
        }

        return this.type;
    }

    @Override
    public String codeGeneration() {
        // Creo una nuova dispatch table
        ArrayList<DispatchTableEntry> dispatchTable = superClassID.equals("")
                ? new ArrayList<>()
                : CodegenUtils.getDispatchTable(superClassID);

        HashMap<String, String> fatherMethods = new HashMap<>();
        for (DispatchTableEntry d : dispatchTable) fatherMethods.put(d.getMethodID(), d.getMethodCode());
        HashMap<String, String> childMethods = new HashMap<>();
        for (MethodNode m : metdeclist) childMethods.put(m.getId(), m.codeGeneration());

        for (int i = 0; i < dispatchTable.size(); i++) {
            String currMethodID = dispatchTable.get(i).getMethodID();
            String redefinedMethodCode = childMethods.get(currMethodID);
            if (redefinedMethodCode != null) {
                dispatchTable.set(i, new DispatchTableEntry(currMethodID, redefinedMethodCode));
            }
        }

        for (MethodNode m : metdeclist) {
            String currMethodID = m.getId();
            if (fatherMethods.get(currMethodID) == null) {
                dispatchTable.add(new DispatchTableEntry(currMethodID, childMethods.get(currMethodID)));
            }
        }

        //Aggiungo sempre la DT anche se è vuota, perchè può capitare di implementare una classe che non ha metodi!
        CodegenUtils.addDispatchTable(classID, dispatchTable);

        return "";
    }

    @Override
    public ArrayList<INode> getChilds() {
        ArrayList<INode> res = new ArrayList<INode>();
        res.addAll(vardeclist);
        res.addAll(metdeclist);
        return res;
    }

    @Override
    public String toString() {
        return superClassID.isEmpty() ? "class " + classID : "class " + classID + " extends " + superClassID;
    }

}
