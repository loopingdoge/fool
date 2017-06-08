package ast;
import java.util.ArrayList;

import util.Environment;
import util.SemanticError;

public class ArrowType implements Type {

    private ArrayList<Type> parlist;
    private Type ret;
  
    public ArrowType(ArrayList<Type> p, Type r) {
        parlist=p;
        ret=r;
    }
    
    public String toPrint(String s) {
	    String parlstr="";
        for (Type par:parlist)
            parlstr+=par.toPrint(s+"  ");
        return s+"ArrowType\n" + parlstr + ret.toPrint(s+"  ->") ;
    }
  
    public Type getRet () {
        return ret;
    }
  
    public ArrayList<Type> getParList () { //
        return parlist;
    }

    @Override
	public ArrayList<SemanticError> checkSemantics(Environment env) {
        return new ArrayList<SemanticError>();
	}


}  