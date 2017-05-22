package ast;

import java.util.ArrayList;

import util.Environment;
import util.SemanticError;
import lib.FOOLlib;

public class IfNode implements Node {

  private Node cond;
  private Node th;
  private Node el;
  
  public IfNode (Node c, Node t, Node e) {
    cond=c;
    th=t;
    el=e;
  }
  
  public String toPrint(String s) {
    return s+"If\n" + cond.toPrint(s+"  ") 
                    + th.toPrint(s+"  ")   
                    + el.toPrint(s+"  "); 
  }
  
  
  @Override
	public ArrayList<SemanticError> checkSemantics(Environment env) {
	  //create the result
	  ArrayList<SemanticError> res = new ArrayList<SemanticError>();
	  
	  //check semantics in the condition
	  res.addAll(cond.checkSemantics(env));
	 	  
	  //check semantics in the then and in the else exp
	  res.addAll(th.checkSemantics(env));
	  res.addAll(el.checkSemantics(env));
	  
	  return res;
	}
  
  
  public Node typeCheck() {
    if (!(FOOLlib.isSubtype(cond.typeCheck(),new BoolTypeNode()))) {
      System.out.println("non boolean condition in if");
      System.exit(0);
    }
    Node t = th.typeCheck();
    Node e = el.typeCheck();
    if (FOOLlib.isSubtype(t,e)) 
      return e;
    if (FOOLlib.isSubtype(e,t))
      return t;
    System.out.println("Incompatible types in then else branches");
    System.exit(0);
    return null;
  }
  
  public String codeGeneration() {
	  String l1 = FOOLlib.freshLabel(); 
	  String l2 = FOOLlib.freshLabel();
	  return cond.codeGeneration()+
			 "push 1\n"+
			 "beq "+ l1 +"\n"+			  
			 el.codeGeneration()+
			 "b " + l2 + "\n" +
			 l1 + ":\n"+
			 th.codeGeneration()+
	         l2 + ":\n"; 
  }
  
}  