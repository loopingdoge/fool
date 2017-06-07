package ast;
import java.util.ArrayList;

import util.Environment;
import util.SemanticError;

public class InstanceType implements Type {

    private Type ret;

    public InstanceType() {}

    public String toPrint(String s) {
        return "";
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        return new ArrayList<SemanticError>();
    }


}