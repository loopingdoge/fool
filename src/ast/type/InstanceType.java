package ast.type;

public class InstanceType implements Type {

    private Type ret;

    @Override
    public TypeID getID() {
        return TypeID.INSTANCE;
    }

    @Override
    public boolean isSubTypeOf(Type t) {
        // TODO: implement
        return false;
    }

    @Override
    public boolean isSuperTypeOf(Type t) {
        // TODO: implement
        return false;
    }

    @Override
    public String toString() {
        // TODO: describe the instance type
        return "Instance";
    }
}