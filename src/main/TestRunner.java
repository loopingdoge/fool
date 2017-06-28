package main;

import exception.LexerException;
import exception.ScopeException;
import exception.TypeException;
import grammar.FOOLLexer;
import grammar.FOOLParser;
import grammar.SVMLexer;
import grammar.SVMParser;
import node.INode;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import symbol_table.Environment;
import type.Type;
import util.CodegenUtils;
import vm.ExecuteVM;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TestRunner {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED    = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_BLUE = "\u001B[34m";
    private static int MEMSIZE;


    private static INode lexicalAndSyntacticAnalysis(CharStream input) throws LexerException {
        FOOLLexer lexer = new FOOLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        if (lexer.lexicalErrors > 0) {
            throw new LexerException("TODO: Qua ci dovrebbe essere un buon messaggio di errore specifico del lexer");
        }
        FOOLParser parser = new FOOLParser(tokens);
        FoolVisitorImpl visitor = new FoolVisitorImpl();
        return visitor.visit(parser.prog()); //generazione AST
    }

    private static Type semanticAnalysis(INode ast, boolean visualizeAST) throws ScopeException, TypeException {
        Environment env = new Environment();
        ArrayList<SemanticError> err = ast.checkSemantics(env);

        if (err.size() > 0) {
            throw new ScopeException(err);
        }

        if (visualizeAST) {
            System.out.println("\nVisualizing AST...");
            stampAST(ast);
        }

        return ast.type(); //type-checking bottom-up
    }

    private static int[] codeGeneration(INode ast, String testID, boolean enableLogging) throws IOException {
        String code = ast.codeGeneration();
        code += CodegenUtils.generateDispatchTablesCode();
        computeMemoryCapacity(code);

        BufferedWriter out = new BufferedWriter(new FileWriter(testID + ".asm"));
        out.write(code);
        out.close();

        if (enableLogging) {
            System.out.println("SVM code generated (" + code.split("\n").length + " lines). Assembling and running generated code: \n" + code);
        }

        CharStream inputASM = CharStreams.fromFileName(testID + ".asm");
        SVMLexer lexerASM = new SVMLexer(inputASM);
        CommonTokenStream tokensASM = new CommonTokenStream(lexerASM);
        SVMParser parserASM = new SVMParser(tokensASM);

        parserASM.assembly();

        if (lexerASM.lexicalErrors > 0) {
            System.err.println("Error: SVM lexer error");
            // TODO: throw new lexer exception
        }
        if (parserASM.getNumberOfSyntaxErrors() > 0) {
            System.err.println("Error: SVM parser error");
            // TODO: throw new parser exception
        }

        if (enableLogging) {
            System.out.println("Code generated! Assembling and running generated code:");
            int[] codeToView = parserASM.getBytecode();
            for (int i = 0; i < codeToView.length; i++) System.out.println(codeToView[i]);
        }

        return parserASM.getBytecode();
    }

//    version with memory counter incrementing by push()
    private static void computeMemoryCapacity(String SVMcode) {
        String[] instructions = SVMcode.split("\n");
        MEMSIZE = 0;
        for (int i = 0; i < instructions.length; i++) {
            String instruction = instructions[i].split(" ")[0];
            // System.out.println("Instruction " + i + " is " + instruction);
            switch (instruction) {
                case "print":
                case "halt":
                case "cfp":
                case "bleq":
                case "beq":
                case "b":
                case "sw":
                case "pop":
                case "js":
                case "sra":
                case "srv":
                case "sfp":
                case "shp":   // no increment
                    break;
                case "new":
                case "lc":
                case "copy":
                case "lra":
                case "lhp":
                case "lrv":
                case "lfp":
                case "lw":
                case "div":
                case "sub":
                case "mult":
                case "add":
                case "push":   // 1 increment
                    MEMSIZE++;
                    break;
                default:   // reached for labels and blank lines
                    // System.out.println("ERROR! Unknown SVM instruction '" + instruction +"' found in computing VM memory.");
                    break;
            }
        }
    }

    private static String executeVM(int[] code) {
        ExecuteVM vm = new ExecuteVM(code, MEMSIZE);
        String message =  "No output";
        try {
            ArrayList<String> output = vm.cpu();
            if (output.size() > 0)
                message = output.get(output.size() - 1);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    public static String test(String testID, CharStream input, String expectedResult, boolean enableLogging) {

        CodegenUtils.reset();
        String actualResult = "";

        try {

            if (enableLogging) {
                System.out.println("Lexer & parser...");
            }

            INode ast = lexicalAndSyntacticAnalysis(input);

            if (enableLogging) {
                System.out.println("Semantic analysis...");
            }

            Type type = semanticAnalysis(ast, enableLogging);

            if (enableLogging) {
                System.out.println("Type: " + type);
            }

            int[] code = codeGeneration(ast, testID, enableLogging);

            if (enableLogging) {
                System.out.println("Starting VM (allocated dimensions: bytecode " + code.length +", memory " + MEMSIZE + ")");
            }

            actualResult = executeVM(code);

            if (enableLogging) {
                System.out.println(actualResult);
            }

        } catch (LexerException | ScopeException | IOException | TypeException e) {
            if (enableLogging) {
                System.out.println(e.getMessage());
            }
            actualResult = e.getMessage();
        }

        StringBuilder output = new StringBuilder();
        output.append( "-Expected: " ).append(expectedResult).append("\n")
                        .append( "-Got: "  ).append(actualResult).append("\n");
        if (actualResult.trim().equals(expectedResult.trim())) {
            output.append( ANSI_GREEN + "Test PASSED!");
        } else {
            output.append( ANSI_RED + "Test FAILED!");
        }

        return output.toString();
    }

    public static void stampAST(INode ast) {
        System.out.println("\nAbstract Syntax Tree NUOVO: ");
        if (ast.getChilds() != null) {
            recursiveStamp(ast, "");
        } else {
            System.out.println("\nEmpty AST");
        }
    }

    private static void recursiveStamp(INode father, String indent) {
        System.out.println(indent + father);
        try {
            for (INode child : father.getChilds()) {
                recursiveStamp(child, indent + "   ");
            }
        } catch (Exception e) {
            System.out.println("Error in -> " + father.getClass());
        }
    }
}
