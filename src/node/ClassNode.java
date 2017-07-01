package node;

import exception.*;
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
    private ArrayList<ParameterNode> attrDecList;
    private ArrayList<MethodNode> metDecList;

    private HashMap<String, Type> fields = new HashMap<>();
    private HashMap<String, FunType> methods = new HashMap<>();

    private ClassType type;

    public ClassNode(ParserRuleContext ctx, String classID, String superClassID, ArrayList<ParameterNode> attrDecList, ArrayList<MethodNode> metDecList) {
        super(ctx);
        this.classID = classID;
        this.superClassID = superClassID;
        this.attrDecList = attrDecList;
        this.metDecList = metDecList;
    }

    public String getClassID() {
        return classID;
    }

    public String getSuperClassID() {
        return superClassID;
    }

    public ArrayList<ParameterNode> getVardeclist() {
        return this.attrDecList;
    }

    public ArrayList<MethodNode> getMetDecList() {
        return metDecList;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {

        ArrayList<SemanticError> res = new ArrayList<>();

        // Usati per creare la entry della classe nella symbol table
        ArrayList<Field> fieldsList = new ArrayList<>();
        ArrayList<Method> methodsList = new ArrayList<>();

        for (ParameterNode var : this.attrDecList) {
            fieldsList.add(new Field(var.getID(), var.getType()));
            fields.put(var.getID(), var.getType());
        }

        for (MethodNode fun : metDecList) { // Per ogni metodo
            ArrayList<Type> paramsType = new ArrayList<>();
            for (ParameterNode param : fun.getParams()) { // Controllo i parametri
                if (param.getType() instanceof InstanceType) { // Se si tratta di oggetti
                    InstanceType paramType = (InstanceType) param.getType();
                    String declaredClass = paramType.getClassType().getClassID();
                    try {
                        ClassType paramClassType = (ClassType) env.getLatestEntryOf(declaredClass).getType();
                        paramsType.add(new InstanceType(paramClassType));
                    } catch (UndeclaredVarException e) {
                        res.add(new SemanticError("Class '" + declaredClass + "' does not exist"));
                    }
                } else { // Se si tratta di valori base
                    paramsType.add(param.getType());
                }
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

        // Creo una entry per la classe nella Symbol Table
        try {
            this.type = new ClassType(classID, superclassType, fieldsList, methodsList);
            env.setEntryType(classID, this.type, 0);
            // env.addEntry(classID, this.type, 0);
            CodegenUtils.addClassEntry(classID, this.type);
        } catch (RedeclaredClassException | UndeclaredClassException e) {
            res.add(new SemanticError(e.getMessage()));
        }

        env.pushHashMap(); // Aggiungo i parametri ad una nuova Symbol Table
        for (ParameterNode var : attrDecList) {
            if (var.getType() instanceof InstanceType) {
                ClassType paramClass = ((InstanceType) var.getType()).getClassType();
                //Controllo che i parametri non siano sottotipo della classe in cui sono
                if (paramClass.isSubTypeOf(this.type))
                    res.add(new SemanticError("can't use a subclass in superclass' constructor"));
            }
            res.addAll(var.checkSemantics(env));
        }

        env.pushHashMap(); // Aggiungo i metodi ad una nuova Symbol Table
        for (MethodNode fun : metDecList) {
            res.addAll(fun.checkSemantics(env));
        }
        env.popHashMap().popHashMap();

        // Se estende una classe
        if (!superClassID.isEmpty()) {
            try {
                if (!(env.getTypeOf(superClassID) instanceof ClassType))
                    res.add(new SemanticError("ID of super class " + superClassID + " is not related to a class type"));
            } catch (UndeclaredVarException exp) {
                res.add(new SemanticError("Super class " + superClassID + " not defined"));
            }

            try {
                ClassType superClassType = (ClassType) env.getLatestEntryOf(superClassID).getType();

                // Se ho almeno tanti attributi quanti ne ha la classe padre
                if (attrDecList.size() >= superClassType.getFields().size()) {
                    for (int i = 0; i < superClassType.getFields().size(); i++) { // per ogni attributo del padre
                        ParameterNode localField = attrDecList.get(i);
                        Field superField = superClassType.getFields().get(i);
                        if (!superField.getID().equals(localField.getID()) // se non hanno lo stesso nome
                            || !localField.getType().isSubTypeOf(superField.getType()) ) {  // o non hanno lo stesso tipo
                            res.add(new SemanticError("Field '" + localField.getID() + "' of class '"+ classID+"' overrided from super class with different type"));
                        }
                    }
                } else {
                    res.add(new SemanticError("Subclass has not the superclass parameters."));
                }
            } catch (UndeclaredVarException e) {
                res.add(new SemanticError("Super class " + superClassID + " not defined " + e.getMessage()));
            }

            try {
                SymbolTableEntry superClassEntry = env.getLatestEntryOf(superClassID);
                ClassType superClassType = (ClassType) superClassEntry.getType();

                HashMap<String, FunType> superClassMethods = superClassType.getMethodsMap();
                for (String localMethod : methods.keySet()) {
                    if (superClassMethods.containsKey(localMethod)) {
                        if (!methods.get(localMethod).isSubTypeOf(superClassMethods.get(localMethod))) {
                            res.add(new SemanticError("Method '" + localMethod + "' of class '" + classID + "' overrided with incompatible type"));
                        }
                    }
                }

            } catch (UndeclaredVarException e) {
                res.add(new SemanticError("Super class " + superClassID + " not defined " + e.getMessage()));
            }
        }

        return res;
    }

    @Override
    public Type type() throws TypeException {

        for (ParameterNode vardec : attrDecList){
            vardec.type();
        }

        for (MethodNode fundec : metDecList) {
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
        for (DispatchTableEntry d : dispatchTable) fatherMethods.put(d.getMethodID(), d.getMethodLabel());
        HashMap<String, String> childMethods = new HashMap<>();
        for (MethodNode m : metDecList) childMethods.put(m.getId(), m.codeGeneration());

        for (int i = 0; i < dispatchTable.size(); i++) {
            String currMethodID = dispatchTable.get(i).getMethodID();
            String redefinedMethodCode = childMethods.get(currMethodID);
            if (redefinedMethodCode != null) {
                dispatchTable.set(i, new DispatchTableEntry(currMethodID, redefinedMethodCode));
            }
        }

        for (MethodNode m : metDecList) {
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
        res.addAll(attrDecList);
        res.addAll(metDecList);
        return res;
    }

    @Override
    public String toString() {
        return superClassID.isEmpty() ? "class " + classID : "class " + classID + " extends " + superClassID;
    }

}
