package ast;

import util.Environment;
import util.SemanticError;

import java.util.ArrayList;

public interface Type {

    String toPrint(String indent);

    ArrayList<SemanticError> checkSemantics(Environment env);

}
