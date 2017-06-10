package ast;

import java.util.ArrayList;

import util.Environment;
import util.SemanticError;

public class PrintNode implements Node {

  private Node val;
  
  public PrintNode (Node v) {
    val=v;
  }

  @Override
  public String toString(){
      return "Print";
  }

  public String toPrint(String s) {
    return s+"Print\n" + val.toPrint(s+"  ") ;
  }
  
  public Type typeCheck() {
    return val.typeCheck();
  }  
  
  @Override
 	public ArrayList<SemanticError> checkSemantics(Environment env) {

 	  return val.checkSemantics(env);
 	}
  
  public String codeGeneration() {
		return val.codeGeneration()+"print\n";
  }

    @Override
    public ArrayList<Node> getChilds() {
        ArrayList<Node> childs = new ArrayList<>();

        childs.add(val);

        return childs;
    }

}  