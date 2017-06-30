package symbol_table;

import exception.RedeclaredClassException;
import exception.RedeclaredVarException;
import exception.UndeclaredClassException;
import exception.UndeclaredVarException;
import type.ClassType;
import type.FunType;
import type.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

public class Environment {

    public int offset = 0;
    private ArrayList<HashMap<String, SymbolTableEntry>> symbolTable = new ArrayList<>();
    private SymbolTableEntry latestClassEntry = null;

    public Environment() {

    }

    public int getNestingLevel() {
        return this.symbolTable.size() - 1;
    }

    public int getCurrentOffset() {
        return this.offset;
    }

    public Environment pushHashMap() {
        this.symbolTable.add(new HashMap<>());
        return this;
    }

    public Environment popHashMap() {
        this.symbolTable.remove(this.symbolTable.size() - 1);
        return this;
    }

    public Environment addEntry(String id, Type type, int offset) throws RedeclaredVarException {
        SymbolTableEntry newEntry = new SymbolTableEntry(getNestingLevel(), type, offset);
        SymbolTableEntry oldEntry = this.symbolTable
                .get(this.symbolTable.size() - 1)
                .put(id, newEntry);
        if (type instanceof ClassType) {
            latestClassEntry = newEntry;
        }
        if (oldEntry != null) {
            throw new RedeclaredVarException(id);
        }
        return this;
    }

    public Environment setEntryType(String id, Type newtype, int offset) throws UndeclaredClassException {
        SymbolTableEntry newEntry = new SymbolTableEntry(getNestingLevel(), newtype, offset);
        SymbolTableEntry  oldEntry = this.symbolTable.get(this.symbolTable.size() - 1).replace(id, newEntry);
        if (newtype instanceof ClassType) {
            latestClassEntry = newEntry;
        }
        if (oldEntry == null) {
            throw   new UndeclaredClassException(id);
        }
        return this;
    }

    public Environment addEntry(String id, Type type, int offset, boolean isInsideClass) throws RedeclaredVarException {
        SymbolTableEntry newEntry = new SymbolTableEntry(getNestingLevel(), type, offset, isInsideClass);
        SymbolTableEntry oldEntry = this.symbolTable
                .get(this.symbolTable.size() - 1)
                .put(id, newEntry);
        if (type instanceof ClassType) {
            latestClassEntry = newEntry;
        }
        if (oldEntry != null) {
            throw new RedeclaredVarException(id);
        }
        return this;
    }

    public SymbolTableEntry getLatestEntryOf(String id) throws UndeclaredVarException {
        ListIterator<HashMap<String, SymbolTableEntry>> li = symbolTable.listIterator(symbolTable.size());
        while (li.hasPrevious()) {
            HashMap<String, SymbolTableEntry> current = li.previous();
            if (current.containsKey(id)) {
                return current.get(id);
            }
        }
        throw new UndeclaredVarException(id);
    }

    public SymbolTableEntry getLatestEntryOfNotFun(String id) throws UndeclaredVarException {
        ListIterator<HashMap<String, SymbolTableEntry>> li = symbolTable.listIterator(symbolTable.size());
        while (li.hasPrevious()) {
            HashMap<String, SymbolTableEntry> current = li.previous();
            if (current.containsKey(id) && !(current.get(id).getType() instanceof FunType)) {
                return current.get(id);
            }
        }
        throw new UndeclaredVarException(id);
    }

    public SymbolTableEntry getLatestClassEntry() throws UndeclaredVarException {
        if (latestClassEntry == null) {
            throw new UndeclaredVarException("symbol table not initialized");
        } else {
            return latestClassEntry;
        }
    }

    public Type getTypeOf(String id) throws UndeclaredVarException {
        return this.getLatestEntryOf(id).getType();
    }

}
