package symbol_table;

import type.Type;

public class SymbolTableEntry {

    private int nestingLevel;
    private Type type;
    private int offset;
    private boolean isInsideClass;

    public SymbolTableEntry(int nestingLevel, Type type, int offset) {
        this.nestingLevel = nestingLevel;
        this.type = type;
        this.offset = offset;
        this.isInsideClass = false;
    }

    public SymbolTableEntry(int nestingLevel, Type type, int offset, boolean isInsideClass) {
        this.nestingLevel = nestingLevel;
        this.type = type;
        this.offset = offset;
        this.isInsideClass = isInsideClass;
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
        return nestingLevel;
    }

    // Used only for class attributes
    public boolean isInsideClass() { return isInsideClass; }

    public String toString(String s) { //
        return "SymbolTableEntry: nestlev " + Integer.toString(nestingLevel) + "\n" +
                "SymbolTableEntry: type " + type + "\n" +
                "SymbolTableEntry: offset " + Integer.toString(offset) + "\n";
    }
}