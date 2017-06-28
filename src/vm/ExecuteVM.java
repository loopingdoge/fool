package vm;

import grammar.SVMParser;

import java.util.ArrayList;

public class ExecuteVM {

    public static final int CODESIZE = 10000;   // TODO: calculate this
    public static final int MEMSIZE = 100;    // TODO: calculate this

    private ArrayList<String> outputBuffer = new ArrayList<>();

    private int[] code;
    private int[] memory = new int[MEMSIZE];

    private int ip = 0;
    private int sp = MEMSIZE;

    private int hp = 0;
    private int fp = MEMSIZE;
    private int ra;
    private int rv;

    private HeapMemory heap = new HeapMemory(20);
    private ArrayList<HeapMemoryCell> heapMemoryInUse = new ArrayList<>();  // TODO: garbage collection

    public ExecuteVM(int[] code) {
        this.code = code;
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
                case SVMParser.STOREW: //
                    address = pop();
                    memory[address] = pop();
                    break;
                case SVMParser.LOADW: // Prende l'indirizzo in cima allo stack e pusha il valore puntato sullo stack
                    push(memory[pop()]);
                    break;
                case SVMParser.BRANCH:
                    address = code[ip];
                    ip = address;
                    break;
                case SVMParser.BRANCHEQ: //
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
                case SVMParser.JS: //
                    address = pop();
                    ra = ip;
                    ip = address;
                    break;
                case SVMParser.STORERA: //
                    ra = pop();
                    break;
                case SVMParser.LOADRA: //
                    push(ra);
                    break;
                case SVMParser.STORERV: //
                    rv = pop();
                    break;
                case SVMParser.LOADRV: //
                    push(rv);
                    break;
                case SVMParser.LOADFP: //
                    push(fp);
                    break;
                case SVMParser.STOREFP: //
                    fp = pop();
                    break;
                case SVMParser.COPYFP: //
                    fp = sp;
                    break;
                case SVMParser.STOREHP: //
                    hp = pop();
                    break;
                case SVMParser.LOADHP: //
                    push(hp);
                    break;
                case SVMParser.PRINT:
                    System.out.println((sp < MEMSIZE) ? memory[sp] : "Empty stack!");
                    outputBuffer.add((sp < MEMSIZE) ? Integer.toString(memory[sp]) : "Empty stack!");
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
                    HeapMemoryCell allocatedMemory = heap.allocate(nargs + 1);
                    // Salvo il blocco di memoria ottenuto per controllarlo in garbage collection
                    heapMemoryInUse.add(allocatedMemory);
                    int heapMemoryStart = allocatedMemory.getIndex();
                    // Inserisco l'indirizzo della dispatch table ed avanzo nella memoria ottenuta
                    memory[allocatedMemory.getIndex()] = dispatchTableAddress;
                    allocatedMemory = allocatedMemory.next;
                    // Inserisco un argument in ogni indirizzo di memoria
                    for (int i = 0; i < nargs; i++) {
                        memory[allocatedMemory.getIndex()] = args[i];
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
                    push(memory[sp]);
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
        int res = memory[sp];
        memory[sp++] = 0;
        return res;
    }

    private void push(int v) {
        memory[--sp] = v;
    }

}