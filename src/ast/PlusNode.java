package ast;

import java.util.ArrayList;

import util.Environment;
import util.SemanticError;
import lib.FOOLlib;

public class PlusNode implements Node {

  private Node left;
  private Node right;
  
  public PlusNode (Node l, Node r) {
    left=l;
    right=r;
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
  
  public String toPrint(String s) {
    return s+"Plus\n" + left.toPrint(s+"  ")  
                      + right.toPrint(s+"  ") ; 
  }
  
  public Node typeCheck() {
    if (! ( FOOLlib.isSubtype(left.typeCheck(),new IntTypeNode()) &&
            FOOLlib.isSubtype(right.typeCheck(),new IntTypeNode()) ) ) {
      System.out.println("Non integers in sum");
      System.exit(0);
    }
    return new IntTypeNode();
  }
  
  public String codeGeneration() {
		return left.codeGeneration()+
			   right.codeGeneration()+
			   "add\n";
  }
  
}  