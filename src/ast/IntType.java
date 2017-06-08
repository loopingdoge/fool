package ast;

import java.util.ArrayList;

import util.Environment;
import util.SemanticError;

public class IntType implements Type {

    public IntType() {}

    public String toPrint(String s) {
	return s+"IntType\n";  
  }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        return new ArrayList<SemanticError>();
    }
  
}  