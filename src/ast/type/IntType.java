package ast.type;

public class IntType implements Type {

    @Override
    public TypeID getID() {
        return TypeID.INT;
    }

    @Override
    public boolean isSubTypeOf(Type t) {
        return this.getID() == t.getID();
    }

    @Override
    public boolean isSuperTypeOf(Type t) {
        return this.getID() == t.getID();
    }

    @Override
    public String toString() {
        return "int";
    }

}