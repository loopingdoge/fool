package ast;

import java.util.ArrayList;

import util.Environment;
import util.SemanticError;

public class BoolType implements Type {

    public BoolType () {}
  
    public String toPrint(String s) {
        return s+"BoolType\n";
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
 	    return new ArrayList<SemanticError>();
 	}

}  