package type;

import exception.UndeclaredMethodException;
import util.Field;
import util.Method;

import java.util.ArrayList;
import java.util.HashMap;

public class ClassType implements Type {

    private String classID = "";
    private ClassType superType = null;

    private ArrayList<Field> fields = new ArrayList<>();
    private ArrayList<Method> methods = new ArrayList<>();

    public ClassType(String classID, ClassType superType, ArrayList<Field> fields, ArrayList<Method> methods) {
        this.classID = classID;
        this.superType = superType;
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
        return superType.getClassID();
    }

    public ClassType getSuperClassType() {
        return superType;
    }

    public ArrayList<Field> getFields() {
        return fields;
    }

    public HashMap<String, Type> getFieldsMap() {
        HashMap<String, Type> fieldsMap = new HashMap<>();
        for (Field f : fields) {
            fieldsMap.put(f.getID(), f.getType());
        }
        return fieldsMap;
    }

    public ArrayList<Method> getMethods() {
        return methods;
    }

    /*
        Returns an HashMap of all methods defined in this class or any subclass
     */
    public HashMap<String, FunType> getMethodsMap() {
        HashMap<String, FunType> methodsMap = new HashMap<>();
        if(superType != null) {
            HashMap<String, FunType> superMethodsMap = superType.getMethodsMap();
            for (String m : superMethodsMap.keySet())
                methodsMap.put(m, superMethodsMap.get(m));
        }
        for (Method m : methods) {
            methodsMap.put(m.getId(), m.getType());
        }
        return methodsMap;
    }

    public int getOffsetOfMethod(String methodID) throws UndeclaredMethodException {
        HashMap<String, Integer> methodsMap = methodsMapFromSuper();
        Integer offset = methodsMap.get(methodID);
        if (offset != null) {
            return offset + 1;
        } else {
            throw new UndeclaredMethodException(methodID);
        }
    }

    public HashMap<String, Integer> methodsMapFromSuper() {
        if (superType == null) {
            HashMap<String, Integer> methodsMap = new HashMap<>();
            for (Method method : methods) {
                methodsMap.put(method.getId(), methodsMap.size());
            }
            return methodsMap;
        } else {
            HashMap<String, Integer> superMethodsMap = superType.methodsMapFromSuper();
            for (Method method : methods) {
                if (!superMethodsMap.containsKey(method.getId())) {
                    superMethodsMap.put(method.getId(), superMethodsMap.size());
                }
            }
            return superMethodsMap;
        }
    }

    public Type getTypeOfMethod(String id) {
        Method method = this.methods
                .stream()
                .filter(m -> m.getId().equals(id))
                .reduce(null, (prev, curr) -> curr);
        if (method != null) {
            return method.getType();
        } else if (superType != null) {
            return superType.getTypeOfMethod(id);
        } else {
            return new VoidType();
        }
    }

    @Override
    public TypeID getID() {
        return TypeID.CLASSDEC;
    }

    @Override
    public boolean isSubTypeOf(Type t2) {
        // Procedo solo se l'altro tipo e' una classe
        if (t2 instanceof ClassType) {
            ClassType ct2 = (ClassType) t2;
            // Se e' della stessa classe
            if (this.getClassID().equals(ct2.getClassID())) {
                return true;
            }
            // Procedo solo se la mia classe ha un supertipo
            if (superType != null) {
                return this.getSuperclassID().equals(ct2.getClassID()) || superType.isSubTypeOf(t2);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "class " + classID;
    }

}