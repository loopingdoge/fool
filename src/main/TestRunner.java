package main;

import ast.FoolVisitorImpl;
import ast.Node;
import ast.Type;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import parser.*;
import util.Environment;
import util.SemanticError;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TestRunner {

    private static Node lexicalAndSyntacticAnalysis(CharStream input) throws LexerException {
        FOOLLexer lexer = new FOOLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        if (lexer.lexicalErrors > 0) {
            throw new LexerException("TODO: Qua ci dovrebbe essere un buon messaggio di errore specifico del lexer");
        }
        FOOLParser parser = new FOOLParser(tokens);
        FoolVisitorImpl visitor = new FoolVisitorImpl();
        return visitor.visit(parser.prog()); //generazione AST
    }

    private static Type semanticAnalysis(Node ast, boolean visualizeAST) throws SemanticException {
        Environment env = new Environment();
        ArrayList<SemanticError> err = ast.checkSemantics(env);

        if (err.size() > 0) {
            throw new SemanticException(err);
        }

        if (visualizeAST) {
            System.out.println("\nVisualizing AST... VECCHIO");
            System.out.println(ast.toPrint(""));
        }

        return ast.typeCheck(); //type-checking bottom-up
    }

    private static int[] codeGeneration(Node ast, String testID, boolean enableLogging) throws IOException {
        String code = ast.codeGeneration();
        BufferedWriter out = new BufferedWriter(new FileWriter(testID + ".asm"));
        out.write(code);
        out.close();

        if (enableLogging) {
            System.out.println("Code generated! Assembling and running generated code.");
        }

        CharStream inputASM = CharStreams.fromFileName(testID + ".asm");
        SVMLexer lexerASM = new SVMLexer(inputASM);
        CommonTokenStream tokensASM = new CommonTokenStream(lexerASM);
        SVMParser parserASM = new SVMParser(tokensASM);
        parserASM.assembly();

        if (lexerASM.lexicalErrors > 0) {
            // TODO: throw new lexer exception
        }
        if (parserASM.getNumberOfSyntaxErrors() > 0) {
            // TODO: throw new parser exception
        }

        return parserASM.code;
    }

    private static String executeVM(int[] code) {
        ExecuteVM vm = new ExecuteVM(code);
        ArrayList<String> output = vm.cpu();
        return output.get(output.size() - 1);
    }

    public static String test(String testID, CharStream input, String expectedResult, boolean enableLogging) {

        String actualResult = "";

        try {

            if (enableLogging) {
                System.out.println("Lexer & parser...");
            }

            Node ast = lexicalAndSyntacticAnalysis(input);

            if (enableLogging) {
                System.out.println("Semantic analysis...");
            }

            //Nuovo metodo di stampa dell'AST
            stampAST(ast);


            Type type = semanticAnalysis(ast, enableLogging);

            if (enableLogging) {
                System.out.println("Type: " + type);
            }

            int[] code = codeGeneration(ast, testID, enableLogging);

            if (enableLogging) {
                System.out.println("Starting VM...");
            }

            actualResult = executeVM(code);

            if (enableLogging) {
                System.out.println(actualResult);
            }

        } catch (LexerException | SemanticException | IOException e) {
            if (enableLogging) {
                System.out.println(e.getMessage());
            }
            actualResult = e.getMessage();
        }

        StringBuilder output = new StringBuilder();
        if (actualResult.equals(expectedResult)) {
            output.append("Test passed!" + "\n");
        } else {
            output.append("Test failed!" + "\n");
        }
        output.append("Expected: ").append(expectedResult).append("\n")
                .append("Got: ").append(actualResult).append("\n");

        return output.toString();
    }

    public static void stampAST(Node ast){
        System.out.println("\nAbstract Syntax Tree NUOVO: ");
        if(ast.getChilds() != null) {
            recursiveStamp(ast, "");
        }else{
            System.out.println("\nEmpty AST");
        }
    }

    //TODO: Mancano all'appello le classi: ProgClassDecNode(che non è finita), MethodNode(incompleta), ThisNode(incompleta)

    public static void recursiveStamp(Node father, String indent){
        System.out.println(indent + father);
        if(father.getChilds() != null){
            for (Node child : father.getChilds()) {
                recursiveStamp(child, indent + "   ");
            }
        }
    }
}
