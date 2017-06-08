package ast;

public class SymbolTableEntry {

    private int nl;
    private Type type;
    private int offset;

    public SymbolTableEntry(int n, int os) {
        nl = n;
        offset = os;
    }

    public SymbolTableEntry(int n, Type t, int os) {
        nl = n;
        type = t;
        offset = os;
    }

    public void addType(Type t) {
        type = t;
    }

    public Type getType() {
        return type;
    }

    public int getOffset() {
        return offset;
    }

    public int getNestinglevel() {
        return nl;
    }

    public String toPrint(String s) { //
        return s + "SymbolTableEntry: nestlev " + Integer.toString(nl) + "\n"
                + s + "SymbolTableEntry: type\n"
                + s + "  " + type + "\n"
                + s + "SymbolTableEntry: offset " + Integer.toString(offset) + "\n";
    }
}