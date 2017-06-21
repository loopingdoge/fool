package type;

public interface Type {

    TypeID getID();

    boolean isSubTypeOf(Type t);

    boolean isSuperTypeOf(Type t);

}
