import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedList;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import parser.ExecuteVM;
import parser.FOOLLexer;
import parser.FOOLParser;
import parser.SVMLexer;
import parser.SVMParser;
import parser.FOOLParser.DecContext;
import parser.FOOLParser.FunContext;
import parser.FOOLParser.FunDeclarationContext;
import parser.FOOLParser.LetContext;
import parser.FOOLParser.LetInExpContext;
import parser.FOOLParser.ProgContext;
import parser.FOOLParser.VardecContext;
import util.Environment;
import util.SemanticError;
import ast.FoolVisitorImpl;
import ast.Node;

public class TestDebug {
    static int count_var (ParseTree t){
        int n = 0 ;
        if (t.getClass().getName().equals("parser.FOOLParser$LetContext")){
            LetContext s = (LetContext) t ;
            for (DecContext dc : s.dec()) {
                if (dc.getClass().getName().equals("parser.FOOLParser$VarAssignmentContext")) {
                    n = n+1 ;
                } else n = n + count_var(dc) ;
            }
            return(n) ;
        }
        else if (t.getClass().getName().equals("parser.FOOLParser$LetInExpContext")){
            LetInExpContext s = (LetInExpContext) t ;
            return(count_var(s.let())) ;
        }
        else if (t.getClass().getName().equals("parser.FOOLParser$FunDeclarationContext")){
            n=n+1 ;
            FunDeclarationContext s = (FunDeclarationContext) t ;
            FunContext r = s.fun();
            for (VardecContext d : r.vardec()) {
                n = n+1 ;
            }
            if (s.fun().let() == null){
                return(n) ;
            } else return (n + count_var(s.fun().let()) );
        }
        else return(0) ;
    }

    static int count_node_ric(ParseTree t){
        int number_of_nodes = 0 ;
        if (t.getChildCount() == 0) return(1) ;
        else {
            for(int i=0; i<t.getChildCount(); i=i+1)
                number_of_nodes = number_of_nodes + count_node_ric(t.getChild(i));
            return(number_of_nodes + 1);
        }
    }

    public static void main(String[] args) throws Exception {

        String filename = "prova.fool";

        CharStream input = CharStreams.fromFileName(filename);
        FOOLLexer lexer = new FOOLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        //SIMPLISTIC BUT WRONG CHECK OF THE LEXER ERRORS
        if (lexer.lexicalErrors > 0){
            System.out.println("The program was not in the right format. Exiting the compilation process now");
        } else {
            FOOLParser parser = new FOOLParser(tokens);
            FoolVisitorImpl visitor = new FoolVisitorImpl();
            Node ast = visitor.visit(parser.prog()); //generazione AST
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
/*
                Node type = ast.typeCheck(); //type-checking bottom-up
                System.out.println(type.toPrint("Type checking ok! Type of the program is: "));

                // CODE GENERATION  prova.fool.asm
                String code=ast.codeGeneration();
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

                if (lexerASM.lexicalErrors>0 || parserASM.getNumberOfSyntaxErrors()>0) {
                    System.exit(1);
                }

                System.out.println("Starting Virtual Machine...");
                ExecuteVM vm = new ExecuteVM(parserASM.code);
                vm.cpu();
*/
            }
        }

    }
}
