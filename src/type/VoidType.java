package type;

public class VoidType implements Type {

    @Override
    public TypeID getID() {
        return TypeID.VOID;
    }

    @Override
    public boolean isSubTypeOf(Type t) {
        return t.getID() == t.getID();
    }

    @Override
    public boolean isSuperTypeOf(Type t) {
        return t.getID() == t.getID();
    }

}
