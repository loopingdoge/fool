package ast.type;

import java.util.HashMap;

public class ClassType implements Type {

    private String classID = "";
    private String superclassID = "";
    private HashMap<String, Type> fields = new HashMap<>();
    private HashMap<String, Type> methods = new HashMap<>();

    public ClassType(String classID, String superclassID, HashMap<String, Type> fields, HashMap<String, Type> methods) {
        this.classID = classID;
        this.superclassID = superclassID;
        this.fields = fields;
        this.methods = methods;
    }

    public ClassType(String classID, String superclassID, HashMap<String, Type> fields) {
        this.classID = classID;
        this.superclassID = superclassID;
        this.fields = fields;
    }

    public ClassType(String classID, String superclassID) {
        this.classID = classID;
        this.superclassID = superclassID;
    }

    public ClassType(String classID) {
        this.classID = classID;
    }

    public String getClassID() {
        return classID;
    }

    public String getSuperclassID() {
        return superclassID;
    }

    public boolean containsID(String id) {
        return this.fields.containsKey(id) || this.methods.containsKey(id);
    }

    public Type getTypeOf(String id) {
        if (this.fields.containsKey(id)) {
            return this.fields.get(id);
        } else if (this.methods.containsKey(id)) {
            return this.methods.get(id);
        } else {
            return new VoidType();
        }
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
    public boolean isSuperTypeOf(Type t) {
        return this.getID().equals(((ClassType) t).getSuperclassID());
    }

    @Override
    public String toString() {
        return "class " + classID;
    }

}