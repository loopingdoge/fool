package ast;

import java.util.ArrayList;

import util.Environment;
import util.SemanticError;

public interface Node {

    String toPrint(String indent);

    ArrayList<SemanticError> checkSemantics(Environment env);

    //  fa il type checking e ritorna:
    //  per una espressione, il suo tipo (oggetto BoolTypeNode o IntType)
    //  per una dichiarazione, "null"
    Type typeCheck();

    String codeGeneration();

    ArrayList<Node> getChilds();

}