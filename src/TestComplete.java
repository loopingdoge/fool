import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import parser.ExecuteVM;
import parser.FOOLLexer;
import parser.FOOLParser;
import parser.SVMLexer;
import parser.SVMParser;
import util.Environment;
import util.SemanticError;
import ast.FoolVisitorImpl;
import ast.Node;
import ast.Type;

public class TestComplete {
    public static void main(String[] args) throws Exception {
      
        String filename = "input.fool";

        CharStream input = CharStreams.fromFileName(filename);
        FOOLLexer lexer = new FOOLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FOOLParser parser = new FOOLParser(tokens);
        FoolVisitorImpl visitor = new FoolVisitorImpl();
        Node ast = visitor.visit(parser.prog()); //generazione AST

        //SIMPLISTIC BUT WRONG CHECK OF THE LEXER ERRORS
        if (lexer.lexicalErrors > 0) {
            System.out.println("The program was not in the right format. Exiting the compilation process now");
        } else {

            Environment env = new Environment();
            ArrayList<SemanticError> err = ast.checkSemantics(env);

            if (err.size() > 0) {
                System.out.println("You had: " + err.size() + " errors:");
                for (SemanticError e : err) {
                    System.out.println("\t" + e);
                }
            } else {
                System.out.println("Visualizing AST...");
                System.out.println(ast.toPrint(""));


		        Type type = ast.typeCheck(); //type-checking bottom-up
		        System.out.println("Type checking ok! Type of the program is: " + type);

                // CODE GENERATION  input.fool.asm
                String code = ast.codeGeneration();
                BufferedWriter out = new BufferedWriter(new FileWriter(filename + ".asm"));
                out.write(code);
                out.close();
                System.out.println("Code generated! Assembling and running generated code.");

                CharStream inputASM = CharStreams.fromFileName(filename + ".asm");
                SVMLexer lexerASM = new SVMLexer(inputASM);
                CommonTokenStream tokensASM = new CommonTokenStream(lexerASM);
                SVMParser parserASM = new SVMParser(tokensASM);

                parserASM.assembly();

                System.out.println(
                        "You had: " + lexerASM.lexicalErrors + " lexical errors and "
                                + parserASM.getNumberOfSyntaxErrors() + " syntax errors."
                );

                if (lexerASM.lexicalErrors > 0 || parserASM.getNumberOfSyntaxErrors() > 0) {
                    System.exit(1);
                }

                System.out.println("Starting Virtual Machine...");
                ExecuteVM vm = new ExecuteVM(parserASM.code);
                vm.cpu();
            }
        }

    }
}
