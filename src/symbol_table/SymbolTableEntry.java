package symbol_table;

import type.Type;

public class SymbolTableEntry {

    private int nestingLevel;
    private Type type;
    private int offset;
    private boolean isAttribute;

    public SymbolTableEntry(int nestingLevel, Type type, int offset) {
        this.nestingLevel = nestingLevel;
        this.type = type;
        this.offset = offset;
        this.isAttribute = false;
    }

    public SymbolTableEntry(int nestingLevel, Type type, int offset, boolean isAttribute) {
        this.nestingLevel = nestingLevel;
        this.type = type;
        this.offset = offset;
        this.isAttribute = isAttribute;
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
    public boolean isAttribute() { return isAttribute; }

    public String toString(String s) { //
        return "SymbolTableEntry: nestlev " + Integer.toString(nestingLevel) + "\n" +
                "SymbolTableEntry: type " + type + "\n" +
                "SymbolTableEntry: offset " + Integer.toString(offset) + "\n";
    }
}