package ast;

import java.util.ArrayList;

import util.Environment;
import util.SemanticError;

public class ClassType implements Type {

    private String classID;
    private String superclassID;
    private ArrayList<Type> fieldsTypes;
    private ArrayList<Type> methodsTypes;

    public ClassType(String classID, String superclassID, ArrayList<Type> fieldsTypes, ArrayList<Type> methodsTypes) {
        this.classID = classID;
        this.superclassID = superclassID;
        this.fieldsTypes = fieldsTypes;
        this.methodsTypes = methodsTypes;
    }

    public ClassType(String classID, String superclassID, ArrayList<Type> fieldsTypes) {
        this.classID = classID;
        this.superclassID = superclassID;
        this.fieldsTypes = fieldsTypes;
    }

    public ClassType(String classID, String superclassID) {

        this.classID = classID;
        this.superclassID = superclassID;
    }

    public ClassType(String classID) {
        this.classID = classID;
    }

    public ArrayList<Type> getFunctionTypes() {
        return methodsTypes;
    }

    public ArrayList<Type> getFieldsTypes() {

        return fieldsTypes;
    }

    public String getClassID() {
        return classID;
    }

    public String getSuperclassID() {
        return superclassID;
    }

    @Override
    public TypeID getID() {
        return TypeID.CLASSDEC;
    }

    @Override
    public boolean isSubTypeOf(Type t) {
        return this.getClassID().equals(((ClassType)t).getClassID()) || this.getSuperclassID().equals(((ClassType)t).getClassID());
    }

    @Override
    public boolean isSuperTypeOf(Type t) { return this.getID().equals(((ClassType)t).getSuperclassID());
    }

    @Override
    public String toString() {
        return "class " + classID;
    }

}