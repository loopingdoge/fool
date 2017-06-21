package type;

import java.util.ArrayList;

public class ArrowType implements Type {

    private ArrayList<Type> params;
    private Type returnType;

    public ArrowType(ArrayList<Type> p, Type r) {
        params = p;
        returnType = r;
    }

    public Type getReturnType() {
        return returnType;
    }

    public ArrayList<Type> getParams() {
        return params;
    }

    @Override
    public TypeID getID() {
        return TypeID.ARROW;
    }

    @Override
    public boolean isSubTypeOf(Type t) {
        return false;
    }

    @Override
    public boolean isSuperTypeOf(Type t) {
        return false;
    }

    @Override
    public String toString() {
        String paramsString = params.stream()
                .map(Object::toString)
                .reduce("", (p1, p2) -> p1.length() != 0 ? p1 + ", " : p1 + p2);
        paramsString = "(" + paramsString + ")";
        return "ArrowType " + paramsString + " -> " + returnType.toString();
    }

}