package ast.type;

public class BoolType implements Type {

    @Override
    public TypeID getID() {
        return TypeID.BOOL;
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
        return "bool";
    }

}