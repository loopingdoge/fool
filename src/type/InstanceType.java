package type;

public class InstanceType implements Type {

    private ClassType classT;

    public InstanceType( ClassType classT ) {
        this.classT = classT;
    }

    @Override
    public TypeID getID() {
        return TypeID.INSTANCE;
    }

    @Override
    public boolean isSubTypeOf(Type t) {
        return classT.isSubTypeOf(t);
    }

    @Override
    public boolean isSuperTypeOf(Type t) {
        return classT.isSuperTypeOf(t);
    }

    @Override
    public String toString() {
        // TODO: describe the instance type
        return "Instance";
    }
}