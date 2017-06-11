package lib;

import parser.SVMParser;

import java.util.ArrayList;

public class ExecuteVM {

    public static final int CODESIZE = 10000;   // TODO: calculate this
    public static final int MEMSIZE = 10000;    // TODO: calculate this

    private ArrayList<String> outputBuffer = new ArrayList<>();

    private int[] code;
    private int[] memory = new int[MEMSIZE];

    private int ip = 0;
    private int sp = MEMSIZE;

    private int hp = 0;
    private int fp = MEMSIZE;
    private int ra;
    private int rv;

    public ExecuteVM(int[] code) {
        this.code = code;
    }

    public ArrayList<String> cpu() {
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
                case SVMParser.LOADW: //
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
                case SVMParser.HALT:
                    return outputBuffer;
            }
        }
    }

    private int pop() {
        return memory[sp++];
    }

    private void push(int v) {
        memory[--sp] = v;
    }

}