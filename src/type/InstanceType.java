package type;

public class InstanceType implements Type {

    private ClassType classT;

    public InstanceType( ClassType classT ) {
        this.classT = classT;
    }

    public ClassType getClassType() {
        return this.classT;
    }

    @Override
    public TypeID getID() {
        return TypeID.INSTANCE;
    }

    @Override
    public boolean isSubTypeOf(Type type) {
        if (type instanceof InstanceType) {
            InstanceType it2 = (InstanceType) type;
            return classT.isSubTypeOf(it2.getClassType());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "instance: " + classT.getClassID();
    }
}