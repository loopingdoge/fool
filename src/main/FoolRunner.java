package main;

import exception.LexerException;
import exception.ParserException;
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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FoolRunner {

    static final String ANSI_RESET = "\u001B[0m";
    static final String ANSI_RED = "\u001B[31m";
    static final String ANSI_GREEN = "\u001B[32m";
    static final String ANSI_BLUE = "\u001B[34m";

    private static int maxMemsizeWithoutRecursion;

    private static INode lexicalAndSyntacticAnalysis(CharStream input) throws LexerException {
        FOOLLexer lexer = new FOOLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        if (lexer.errors.size() > 0) {
            throw new LexerException(lexer.errors);
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
            System.out.println("Abstract Syntax Tree:");
            System.out.println(myPrintAST(ast, ""));
        }

        return ast.type(); //type-checking bottom-up
    }

    private static int[] codeGeneration(INode ast, String svmFilename, String bytecodeFilename, boolean keepSvmFile, boolean enableLogging) throws IOException, LexerException, ParserException {
        String code = ast.codeGeneration();
        code += CodegenUtils.generateDispatchTablesCode();
        computeMemoryCapacity(code);

        File svmFile = new File(svmFilename);
        BufferedWriter svmWriter = new BufferedWriter(new FileWriter(svmFile.getAbsoluteFile()));
        svmWriter.write(code);
        svmWriter.close();

        if (enableLogging) {
            System.out.println("SVM code generated (" + code.split("\n").length + " lines). Assembling and running generated code: \n" + code);
        }

        CharStream inputASM = CharStreams.fromFileName(svmFile.getAbsolutePath());
        SVMLexer lexerASM = new SVMLexer(inputASM);
        CommonTokenStream tokensASM = new CommonTokenStream(lexerASM);
        SVMParser parserASM = new SVMParser(tokensASM);

        parserASM.assembly();

        if (lexerASM.errors.size() > 0) {
            throw new LexerException(lexerASM.errors);
        }
        if (parserASM.getNumberOfSyntaxErrors() > 0) {
            throw new ParserException("SVM parser error");
        }

        if (enableLogging) {
            System.out.println("Code generated! Assembling and running generated code:");
            int[] codeToView = parserASM.getBytecode();
            for (int aCodeToView : codeToView) System.out.println(aCodeToView);
        }

        if (!keepSvmFile) {
            svmFile.delete();
        }

        if (!bytecodeFilename.equals("")) {
            File bytecodeFile = new File(bytecodeFilename);
            BufferedWriter bytecodeWriter = new BufferedWriter(new FileWriter(bytecodeFile.getAbsoluteFile()));
            bytecodeWriter.write(code);
            bytecodeWriter.close();
        }

        return parserASM.getBytecode();
    }

    //    version with memory counter incrementing by push()
    private static void computeMemoryCapacity(String SVMcode) {
        String[] instructions = SVMcode.split("\n");
        maxMemsizeWithoutRecursion = 0;
        for (String instruction1 : instructions) {
            String instruction = instruction1.split(" ")[0];
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
                    maxMemsizeWithoutRecursion++;
                    break;
                default:   // reached for labels and blank lines
                    // System.out.println("ERROR! Unknown SVM instruction '" + instruction +"' found in computing VM memory.");
                    break;
            }
        }
    }

    private static String executeVM(int[] code, boolean debug) {
        ExecuteVM vm = new ExecuteVM(code, debug);
        String message = "No output";
        ArrayList<String> output = vm.cpu();
        if (output.size() > 0)
            message = output.get(output.size() - 1);
        return message;
    }

    public static String run(CharStream input, String svmFilename, String bytecodeFilename, boolean keepSvmFile, boolean enableLogging, boolean showAST, boolean noColors) {
        CodegenUtils.reset();
        String result = "";
        try {

            if (enableLogging) {
                System.out.println("Lexer & parser...");
            }

            INode ast = lexicalAndSyntacticAnalysis(input);

            if (enableLogging) {
                System.out.println("Semantic analysis...");
            }

            Type type = semanticAnalysis(ast, showAST);

            System.out.println("Type: " + type);

            int[] code = codeGeneration(ast, svmFilename, bytecodeFilename, keepSvmFile, enableLogging);

            if (enableLogging) {
                System.out.println("Starting VM (allocated dimensions: bytecode " + code.length + ", memory " + maxMemsizeWithoutRecursion + ")");
            }

            result = executeVM(code, enableLogging);

        } catch (LexerException | ScopeException | IOException | TypeException | ParserException e) {
            if (enableLogging) {
                System.out.println(e.getMessage());
            }
            result = e.getMessage();
        }
        return result;
    }

    public static String test(String testID, CharStream input, String expectedResult, boolean enableLogging, boolean showAST, boolean noColors) {
        String actualResult = "";

        try {
            actualResult = run(input, testID + ".svm", "", false, enableLogging, showAST, noColors);
        } catch(Exception e) {
            e.printStackTrace();
        }
        StringBuilder output = new StringBuilder();
        output.append("-Expected: ").append(expectedResult).append("\n")
                .append("-Got: ").append(actualResult).append("\n");
        if (actualResult.trim().equals(expectedResult.trim())) {
            output.append(noColors ? "" : ANSI_GREEN)
                    .append("Test PASSED!");
        } else {
            output.append(noColors ? "" : ANSI_RED)
                    .append("Test FAILED!");
        }

        return output.toString();
    }

    private static String myPrintAST(INode ast, String indent) {
        StringBuilder res = new StringBuilder(indent + ast + "\n");
        for (INode child : ast.getChilds()) {
            res.append(myPrintAST(child, indent + "    "));
        }
        return res.toString();
    }

}
