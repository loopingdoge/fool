package type;

import util.Field;
import util.Method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class ClassType implements Type {

    private String classID = "";
    private String superclassID = "";

    private ArrayList<Field> fields = new ArrayList<>();
    private ArrayList<Method> methods = new ArrayList<>();

    public ClassType(String classID, String superclassID, ArrayList<Field> fields, ArrayList<Method> methods) {
        this.classID = classID;
        this.superclassID = superclassID;
        this.fields = fields;
        this.methods = methods;
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

    public ArrayList<Field> getFields() {
        return fields;
    }

    public HashMap<String, Type> getFieldsMap() {
        HashMap<String, Type> fieldsMap = new HashMap<>();
        for (Field f : fields) {
            fieldsMap.put(f.getId(), f.getType());
        }
        return fieldsMap;
    }

    public ArrayList<Method> getMethods() {
        return methods;
    }

    public HashMap<String, FunType> getMethodsMap() {
        HashMap<String, FunType> methodsMap = new HashMap<>();
        for (Method m : methods) {
            methodsMap.put(m.getId(), m.getType());
        }
        return methodsMap;
    }

    public boolean containsID(String id) {
        return Math.min(
                fields.stream()
                        .filter(field -> field.getId().equals(id))
                        .collect(Collectors.toList())
                        .size()
                ,
                methods.stream()
                        .filter(method -> method.getId().equals(id))
                        .collect(Collectors.toList())
                        .size()
        ) > 0;
    }

    public Type getTypeOfField(String id) {
        Field field = this.fields
                .stream()
                .filter(f -> f.getId().equals(id))
                .reduce(null, (prev, curr) -> curr);
        if (field != null) {
            return field.getType();
        } else {
            return new VoidType();
        }
    }

    public Type getTypeOfMethod(String id) {
        Field method = this.fields
                .stream()
                .filter(m -> m.getId().equals(id))
                .reduce(null, (prev, curr) -> curr);
        if (method != null) {
            return method.getType();
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