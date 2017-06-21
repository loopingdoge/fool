package symbol_table;

import type.Type;

public class SymbolTableEntry {

    private int nestingLevel;
    private Type type;
    private int offset;

    public SymbolTableEntry(int nestingLevel, Type type, int offset) {
        this.nestingLevel = nestingLevel;
        this.type = type;
        this.offset = offset;
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

    public String toPrint(String s) { //
        return s + "SymbolTableEntry: nestlev " + Integer.toString(nestingLevel) + "\n"
                + s + "SymbolTableEntry: type " + type + "\n"
                + s + "SymbolTableEntry: offset " + Integer.toString(offset) + "\n";
    }
}