package ast;

enum TypeID {
    INT,
    BOOL,
    ARROW,
    INSTANCE
}

public interface Type {

    TypeID getID();

    boolean isSubTypeOf(Type t);

    boolean isSuperTypeOf(Type t);

}
