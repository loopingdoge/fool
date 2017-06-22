package type;

import java.util.ArrayList;

public class FunType implements Type {

    private ArrayList<Type> params;
    private Type returnType;

    public FunType(ArrayList<Type> p, Type r) {
        params = p;
        returnType = r;
    }

    public Type getReturnType() {
        return returnType;
    }

    public ArrayList<Type> getParams() {
        return params;
    }

    @Override
    public TypeID getID() {
        return TypeID.FUN;
    }

    @Override
    public boolean isSuperTypeOf(Type t) {
        if (t instanceof FunType) {
            FunType funType = (FunType) t;
            boolean check = true;

            //Se hanno lo stesso numero di parametri
            if (this.params.size() == funType.getParams().size()) {
                //Controllo che tutti i parametri abbiano lo stesso tipo(supertype, come da cosegna)
                for (int i = 0; i < this.params.size(); i++) {
                    check &= funType.getParams().get(i).isSuperTypeOf(this.params.get(i));
                }

                //Controllo che anche il valore di ritorno della funzione
                check &= funType.returnType.isSubTypeOf(this.returnType);
            } else {
                check = false;
            }
            return check;
        } else {
            return false;
        }
    }

    @Override
    public boolean isSubTypeOf(Type t) {
        if (t instanceof FunType) {
            FunType funType = (FunType) t;
            boolean check = true;

            //Se hanno lo stesso numero di parametri
            if (this.params.size() == funType.getParams().size()) {
                //Controllo che tutti i parametri abbiano lo stesso tipo(supertype, come da cosegna)
                for (int i = 0; i < this.params.size(); i++) {
                    check &= funType.getParams().get(i).isSubTypeOf(this.params.get(i));
                }

                //Controllo che anche il valore di ritorno della funzione
                check &= funType.returnType.isSuperTypeOf(this.returnType);
            } else {
                check = false;
            }
            return check;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        String paramsString = params.stream()
                .map(Object::toString)
                .reduce("", (p1, p2) -> p1.length() != 0 ? p1 + ", " : p1 + p2);
        paramsString = "(" + paramsString + ")";
        return "FunType " + paramsString + " -> " + returnType.toString();
    }
}