package ast;

import lib.FOOLlib;
import util.Environment;
import util.SemanticError;

import java.util.ArrayList;

public class OrNode implements Node {

    private Node left;
    private Node right;

    public OrNode(Node l, Node r) {
        left=l;
        right=r;
    }
  
    public String toPrint(String s) {
        return s+"Or\n" + left.toPrint(s+"  ")
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
            System.out.println("Incompatible types in OR");
            System.exit(0);
        }
        //Non serve perchè in teoria se dà errore lo da sopra nel controllo dei SubType
        if(l.getClass() != ast.BoolType.class){
            System.out.println("Operator '||' cannot be applied to 'int', 'int'");
            System.exit(0);
        }
        return new BoolType();
    }
  
    public String codeGeneration() {
        String l1 = FOOLlib.freshLabel();
	    String l2 = FOOLlib.freshLabel();
	    return left.codeGeneration()+
                "push 1\n" +
                "beq " + l1 + "\n" +
                right.codeGeneration()+
                "push 1\n" +
                "beq "+ l1 +"\n"+
			   "push 0\n"+
			   "b " + l2 + "\n" +
			   l1 + ":\n"+
			   "push 1\n" +
			   l2 + ":\n";
    }

    @Override
    public String toString(){
        return "Or";
    }

    @Override
    public ArrayList<Node> getChilds() {
        ArrayList<Node> childs = new ArrayList<>();

        childs.add(left);
        childs.add(right);

        return childs;
    }

}  