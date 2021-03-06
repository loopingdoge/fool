package type;

import exception.UndeclaredClassException;
import exception.UndeclaredVarException;
import main.SemanticError;
import symbol_table.Environment;

import java.util.ArrayList;

public class InstanceType implements Type {

    private ClassType classT;

    public InstanceType( ClassType classT ) {
        this.classT = classT;
    }

    public ClassType getClassType() {
        return this.classT;
    }

    // This is used to update the classType filling superType when needed
    public ArrayList<SemanticError> updateClassType(Environment env) {
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();
        try {
            try {
                this.classT = (ClassType) env.getTypeOf(classT.getClassID());
            } catch (UndeclaredVarException e) {
                throw new UndeclaredClassException(classT.getClassID());
            }
        } catch (UndeclaredClassException e) {
            res.add(new SemanticError(e.getMessage()));
        }
        return res;
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