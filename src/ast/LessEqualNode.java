package ast;

import lib.FOOLlib;
import parser.FOOLLexer;
import util.Environment;
import util.SemanticError;

import java.util.ArrayList;

public class LessEqualNode implements Node {

    private Node left;
    private Node right;

    public LessEqualNode(Node l, Node r) {
        left=l;
        right=r;
    }
  
    public String toPrint(String s) {
        return s+"LessEqual\n" + left.toPrint(s+"  ")
                + right.toPrint(s+"  ") ;
    }
  
    @Override
	public ArrayList<SemanticError> checkSemantics(Environment env) {
	    //create the result
	    ArrayList<SemanticError> res = new ArrayList<SemanticError>();
	  
	    //check semantics in the left and in the right exp
	  
	    res.addAll(left.checkSemantics(env));
	    res.addAll(right.checkSemantics(env));
	  
	    return res;
	}
  
    public Type typeCheck() {
        Type l = left.typeCheck();
        Type r = right.typeCheck();
        if (! ( FOOLlib.isSubtype(l,r) || FOOLlib.isSubtype(r,l) ) ) {
            System.out.println("Incompatible types in lessequal");
            System.exit(0);
        }
        //Ho messo qua il controllo che non sia un operazione invalida, non so se va qua però
        if(l.getClass() == ast.BoolType.class){
            System.out.println("Operator '<=' cannot be applied to 'boolean', 'boolean'");
            System.exit(0);

        }
        return new BoolType();
    }
  
    public String codeGeneration() {
        String l1 = FOOLlib.freshLabel();
	    String l2 = FOOLlib.freshLabel();
	    return left.codeGeneration()+
                right.codeGeneration()+
                "bleq "+ l1 +"\n"+
			   "push 0\n"+
			   "b " + l2 + "\n" +
			   l1 + ":\n"+
			   "push 1\n" +
			   l2 + ":\n";
    }
  
}  