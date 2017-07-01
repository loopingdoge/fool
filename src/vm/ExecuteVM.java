package vm;

import exception.VMOutOfMemoryException;
import grammar.SVMParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ExecuteVM {

    public static final int MEMORY_START_ADDRESS = 0;
    private ArrayList<String> outputBuffer = new ArrayList<>();
    private int memsize = 10000;
    private int[] memory;

    private int[] code;

    private int hp = 0;
    private int ip = 0;
    private int sp;
    private int fp;
    private int ra;
    private int rv;

    private HeapMemory heap;
    private HashSet<HeapMemoryCell> heapMemoryInUse = new HashSet<>();

    public ExecuteVM(int[] code) {
        this.code = code;
        this.memory = new int[memsize];
        this.heap = new HeapMemory(memsize);
        this.sp = MEMORY_START_ADDRESS + memsize;
        this.fp = MEMORY_START_ADDRESS + memsize;
    }

    private int accessMemory(int address) {
        return memory[address - MEMORY_START_ADDRESS];
    }

    private void setMemory(int address, int value) {
        memory[address - MEMORY_START_ADDRESS] = value;
    }

    // Mark and sweep
    private void garbageCollection() {
        // address => isUsed
        HashMap<Integer, Boolean> table = new HashMap<>();
        // Inizializzo a false tutti gli oggetti
        for (HeapMemoryCell m : heapMemoryInUse) {
            table.put(m.getIndex(), false);
        }
        // Se viene trovato sullo stack l'indirizzo di un oggetto, setto la table a true
        for (int i = 99; i >= sp; i--) {
            if (table.containsKey(accessMemory(i))) {
                table.put(accessMemory(i), true);
            }
        }
        heapMemoryInUse.forEach(m -> {
            if (!table.get(m.getIndex())) heap.deallocate(m);
            while (m != null) {
                setMemory(m.getIndex(), 0);
                m = m.next;
            }
        });
        heapMemoryInUse.removeIf(m -> !table.get(m.getIndex()));
    }

    private void printMemory() {
        for (int mem : memory)
            System.out.print(mem + " ");
        System.out.println();
        System.out.println("sp: " + sp + "  fp: " + fp + "  ra: " + ra + "  rv: " + rv + "  hp: " + hp);
        System.out.println();
    }

    public ArrayList<String> cpu() {
        boolean debug = false;
        if (debug) {
            System.out.println("start :");
            printMemory();
        }
        while (true) {
            int bytecode = code[ip++]; // fetch
            int v1, v2;
            int address;
            switch (bytecode) {
                case SVMParser.PUSH:
                    push(code[ip++]);
                    break;
                case SVMParser.POP:
                    pop();
                    break;
                case SVMParser.ADD:
                    v1 = pop();
                    v2 = pop();
                    push(v2 + v1);
                    break;
                case SVMParser.MULT:
                    v1 = pop();
                    v2 = pop();
                    push(v2 * v1);
                    break;
                case SVMParser.DIV:
                    v1 = pop();
                    v2 = pop();
                    push(v2 / v1);
                    break;
                case SVMParser.SUB:
                    v1 = pop();
                    v2 = pop();
                    push(v2 - v1);
                    break;
                case SVMParser.STOREW:
                    address = pop();
                    setMemory(address, pop());
                    break;
                case SVMParser.LOADW: // Prende l'indirizzo in cima allo stack e pusha il valore puntato sullo stack
                    push(accessMemory(pop()));
                    break;
                case SVMParser.BRANCH:
                    address = code[ip];
                    ip = address;
                    break;
                case SVMParser.BRANCHEQ:
                    address = code[ip++];
                    v1 = pop();
                    v2 = pop();
                    if (v2 == v1) ip = address;
                    break;
                case SVMParser.BRANCHLESSEQ:
                    address = code[ip++];
                    v1 = pop();
                    v2 = pop();
                    if (v2 <= v1) ip = address;
                    break;
                case SVMParser.JS:
                    address = pop();
                    ra = ip;
                    ip = address;
                    break;
                case SVMParser.STORERA:
                    ra = pop();
                    break;
                case SVMParser.LOADRA: //
                    push(ra);
                    break;
                case SVMParser.STORERV:
                    rv = pop();
                    break;
                case SVMParser.LOADRV:
                    push(rv);
                    break;
                case SVMParser.LOADFP:
                    push(fp);
                    break;
                case SVMParser.STOREFP:
                    fp = pop();
                    break;
                case SVMParser.COPYFP:
                    fp = sp;
                    break;
                case SVMParser.STOREHP:
                    hp = pop();
                    break;
                case SVMParser.LOADHP:
                    push(hp);
                    break;
                case SVMParser.PRINT:
                    System.out.println((sp < MEMORY_START_ADDRESS + memsize) ? accessMemory(sp) : "Empty stack!");
                    outputBuffer.add((sp < MEMORY_START_ADDRESS + memsize) ? Integer.toString(accessMemory(sp)) : "Empty stack!");
                    break;
                case SVMParser.NEW:
                    // Il numero di argomenti per il new e' sulla testa dello stack
                    int dispatchTableAddress = pop();
                    int nargs = pop();
                    int[] args = new int[nargs];
                    // Poppo gli argomenti
                    for (int i = nargs - 1; i >= 0; i--) {
                        args[i] = pop();
                    }
                    // Alloco memoria per i nargs argomenti + 1 per l'indirizzo alla dispatch table
                    HeapMemoryCell allocatedMemory = null;
                    try {
                        allocatedMemory = heap.allocate(nargs + 1);
                    } catch (VMOutOfMemoryException e) {
                        garbageCollection();
                        try {
                            allocatedMemory = heap.allocate(nargs + 1);
                        } catch (VMOutOfMemoryException e1) {
                            outputBuffer.add("VM out of memory");
                            return outputBuffer;
                        }
                    }
                    // Salvo il blocco di memoria ottenuto per controllarlo in garbage collection
                    heapMemoryInUse.add(allocatedMemory);
                    int heapMemoryStart = allocatedMemory.getIndex();
                    // Inserisco l'indirizzo della dispatch table ed avanzo nella memoria ottenuta
                    setMemory(allocatedMemory.getIndex(), dispatchTableAddress);
                    allocatedMemory = allocatedMemory.next;
                    // Inserisco un argument in ogni indirizzo di memoria
                    for (int i = 0; i < nargs; i++) {
                        setMemory(allocatedMemory.getIndex(), args[i]);
                        allocatedMemory = allocatedMemory.next;
                    }
                    // Metto sullo stack l'indirizzo della prima cella dell'oggetto che ho istanziato
                    push(heapMemoryStart);
                    // A questo punto dovrei aver usato tutta la memoria allocata
                    assert allocatedMemory == null;
                    hp = heap.getNextFreeAddress();
                    break;
                case SVMParser.LC:
                    int codeAddress = pop();
                    push(code[codeAddress]);
                    break;
                case SVMParser.COPY:
                    push(accessMemory(sp));
                    break;
                case SVMParser.HOFF:
                    int objAddress = accessMemory(sp); // indirizzo di this
                    accessMemory(sp + 1); // offset dell'oggetto rispetto al coso

                    break;
                case SVMParser.HALT:
                    return outputBuffer;
            }
            if (debug) {
                System.out.println(bytecode + ": ");
                printMemory();
            }
        }
    }

    private int pop() {
        int res = accessMemory(sp);
        setMemory(sp++, 0);
        return res;
    }

    private void push(int v) {
        setMemory(--sp, v);
    }

}