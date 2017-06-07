package ast;

import lib.FOOLlib;
import util.Environment;
import util.SemanticError;

import java.util.ArrayList;

public class GreaterEqualNode implements Node {

    private Node left;
    private Node right;

    public GreaterEqualNode(Node l, Node r) {
        left=l;
        right=r;
    }
  
    public String toPrint(String s) {
        return s+"GreaterEqual\n" + left.toPrint(s+"  ")
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
  
    public Node typeCheck() {
        Node l = left.typeCheck();
        Node r = right.typeCheck();
        if (! ( FOOLlib.isSubtype(l,r) || FOOLlib.isSubtype(r,l) ) ) {
            System.out.println("Incompatible types in greaterequal");
            System.exit(0);
        }
        //Ho messo qua il controllo che non sia un operazione invalida, non so se va qua però
        if(l.getClass() == ast.BoolTypeNode.class){
            System.out.println("Operator '>=' cannot be applied to 'boolean', 'boolean'");
            System.exit(0);

        }
        return new BoolTypeNode();
    }
  
    public String codeGeneration() {
        String l1 = FOOLlib.freshLabel();
	    String l2 = FOOLlib.freshLabel();
	    //Dal Less Equal basta invertire l'ordine dei due valori nello stack iniziale su cui andrà a valutare il 'bleq'
	    return right.codeGeneration()+
                left.codeGeneration()+
                "bleq "+ l1 +"\n"+
			   "push 0\n"+
			   "b " + l2 + "\n" +
			   l1 + ":\n"+
			   "push 1\n" +
			   l2 + ":\n";
    }
  
}  