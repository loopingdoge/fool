package ast;

enum TypeID {
    INT,
    BOOL,
    ARROW,
    CLASSDEC,
    INSTANCE
}

public interface Type {

    TypeID getID();

    boolean isSubTypeOf(Type t);

    boolean isSuperTypeOf(Type t);

}
