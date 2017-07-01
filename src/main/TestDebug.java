package main;

import grammar.FOOLParser.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.ParseTree;

public class TestDebug {

    static int count_var(ParseTree t) {
        int n = 0;
        if (t.getClass().getName().equals("parser.FOOLParser$LetContext")) {
            LetContext s = (LetContext) t;
            for (DecContext dc : s.dec()) {
                if (dc.getClass().getName().equals("parser.FOOLParser$VarAssignmentContext")) {
                    n = n + 1;
                } else n = n + count_var(dc);
            }
            return (n);
        } else if (t.getClass().getName().equals("parser.FOOLParser$LetInExpContext")) {
            LetInExpContext s = (LetInExpContext) t;
            return (count_var(s.let()));
        } else if (t.getClass().getName().equals("parser.FOOLParser$FunDeclarationContext")) {
            n = n + 1;
            FunDeclarationContext s = (FunDeclarationContext) t;
            FunContext r = s.fun();
            for (VardecContext d : r.vardec()) {
                n = n + 1;
            }
            if (s.fun().let() == null) {
                return (n);
            } else return (n + count_var(s.fun().let()));
        } else return (0);
    }

    static int count_node_ric(ParseTree t) {
        int number_of_nodes = 0;
        if (t.getChildCount() == 0) return (1);
        else {
            for (int i = 0; i < t.getChildCount(); i = i + 1)
                number_of_nodes = number_of_nodes + count_node_ric(t.getChild(i));
            return (number_of_nodes + 1);
        }
    }

    public static void main(String[] args) throws Exception {
        String filename = "input.fool";
        CharStream input = CharStreams.fromFileName(filename);
        String output = FoolRunner.test("", input, "", true, true);
    }
}
