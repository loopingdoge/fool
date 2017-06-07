package ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import parser.*;
import parser.FOOLParser.BaseExpContext;
import parser.FOOLParser.BoolValContext;
import parser.FOOLParser.DecContext;
import parser.FOOLParser.ExpContext;
import parser.FOOLParser.FactorContext;
import parser.FOOLParser.FunContext;
import parser.FOOLParser.FunExpContext;
import parser.FOOLParser.IfExpContext;
import parser.FOOLParser.IntValContext;
import parser.FOOLParser.LetInExpContext;
import parser.FOOLParser.SingleExpContext;
import parser.FOOLParser.TermContext;
import parser.FOOLParser.TypeContext;
import parser.FOOLParser.VarExpContext;
import parser.FOOLParser.VarasmContext;
import parser.FOOLParser.VardecContext;
import util.SemanticError;

public class FoolVisitorImpl extends FOOLBaseVisitor<Node> {

    @Override
    public Node visitSingleExp(SingleExpContext ctx) {
        return visit(ctx.exp());    //simply return the result of the visit to the inner exp
    }

	@Override
	public Node visitLetInExp(LetInExpContext ctx) {
		//resulting node of the right type
		ProgLetInNode res;
		
		//list of declarations in @res
		ArrayList<Node> declarations = new ArrayList<Node>();
		
		//visit all nodes corresponding to declarations inside the let context and store them in @declarations
		//notice that the ctx.let().dec() method returns a list, this is because of the use of * or + in the grammar
		//antlr detects this is a group and therefore returns a list
		for(DecContext dc : ctx.let().dec()){
			declarations.add( visit(dc) );
		}
		
		//visit exp context
		Node exp = visit( ctx.exp() );
		
		//build @res accordingly with the result of the visits to its content
		res = new ProgLetInNode(declarations,  exp);
		
		return res;
	}

    @Override
    public Node visitClassExp(FOOLParser.ClassExpContext ctx) {
        // TODO: implement
        ProgClassDecNode res = null;
        System.err.println("Implement visitClassExp in FoolVisitorImpl.java");
        return res;
    }

	@Override
	public Node visitVarasm(VarasmContext ctx) {
		//declare the result node
		VarNode result;
		
		//visit the type
		Node typeNode = visit(ctx.vardec().type());
		
		//visit the exp
		Node expNode = visit(ctx.exp());
		
		//build the varNode
		return new VarNode(ctx.vardec().ID().getText(), typeNode, expNode);
	}
	
	@Override
	public Node visitFun(FunContext ctx) {
		
		//initialize @res with the visits to the type and its ID
		FunNode res = new FunNode(ctx.ID().getText(), visit(ctx.type()));
		
		//add argument declarations
		//we are getting a shortcut here by constructing directly the ParNode
		//this could be done differently by visiting instead the VardecContext
		for(VardecContext vc : ctx.vardec())
			res.addPar( new ParNode(vc.ID().getText(), visit( vc.type() )) );
		
		//add body
		//create a list for the nested declarations
		ArrayList<Node> innerDec = new ArrayList<Node>();
		
		//check whether there are actually nested decs
		if(ctx.let() != null){
			//if there are visit each dec and add it to the @innerDec list
			for(DecContext dc : ctx.let().dec())
				innerDec.add(visit(dc));
		}
		
		//get the exp body
		Node exp = visit(ctx.exp());
		
		//add the body and the inner declarations to the function
		res.addDecBody(innerDec, exp);
		
		return res;		
		
	}
	
	@Override
	public Node visitType(TypeContext ctx) {
		if(ctx.getText().equals("int"))
			return new IntTypeNode();
		else if(ctx.getText().equals("bool"))
			return new BoolTypeNode();
		
		//this will never happen thanks to the parser
		return null;

	}
	
	@Override
	public Node visitExp(ExpContext ctx) {
		//this could be enhanced

		//check whether this is a simple or binary expression
		//notice here the necessity of having named elements in the grammar
		if(ctx.right == null){
			//it is a simple expression
			return visit( ctx.left );
		}else{

            //it is a binary expression, you should visit left and right
            if (ctx.operator.getType() == FOOLLexer.PLUS) {
                return new PlusNode(visit(ctx.left), visit(ctx.right));
            } else {
                return new MinusNode(visit(ctx.left), visit(ctx.right));
            }
		}
		
	}
	
	@Override
	public Node visitTerm(TermContext ctx) {
		//check whether this is a simple or binary expression
		//notice here the necessity of having named elements in the grammar
		if(ctx.right == null){
			//it is a simple expression
			return visit( ctx.left );
		}else{
			//it is a binary expression, you should visit left and right

			return new MultNode(visit(ctx.left), visit(ctx.right));
		}
	}
	
	
	@Override
	public Node visitFactor(FactorContext ctx) {
		//check whether this is a simple or binary expression
		//notice here the necessity of having named elements in the grammar
		if(ctx.right == null){
			//it is a simple expression
			return visit( ctx.left );
		}else{
			//it is a binary expression, you should visit left and right
			return new EqualNode(visit(ctx.left), visit(ctx.right));
		}
	}
	
	@Override
	public Node visitIntVal(IntValContext ctx) {
		// notice that this method is not actually a rule but a named production #intVal
		
		//there is no need to perform a check here, the lexer ensures this text is an int
		return new IntNode(Integer.parseInt(ctx.INTEGER().getText()));
	}
	
	@Override
	public Node visitBoolVal(BoolValContext ctx) {
		
		//there is no need to perform a check here, the lexer ensures this text is a boolean
		return new BoolNode(Boolean.parseBoolean(ctx.getText())); 
	}
	
	@Override
	public Node visitBaseExp(BaseExpContext ctx) {
		
		//this is actually nothing in the sense that for the ast the parenthesis are not relevant
		//the thing is that the structure of the ast will ensure the operational order by giving
		//a larger depth (closer to the leafs) to those expressions with higher importance
		
		//this is actually the default implementation for this method in the FOOLBaseVisitor class
		//therefore it can be safely removed here
		
		return visit (ctx.exp());

	}
	
	@Override
	public Node visitIfExp(IfExpContext ctx) {
		
		//create the resulting node
		IfNode res;
		
		//visit the conditional, then the then branch, and then the else branch
		//notice once again the need of named terminals in the rule, this is because
		//we need to point to the right expression among the 3 possible ones in the rule
		
		Node condExp = visit (ctx.cond);
		
		Node thenExp = visit (ctx.thenBranch);
		
		Node elseExp = visit (ctx.elseBranch);
		
		//build the @res properly and return it
		res = new IfNode(condExp, thenExp, elseExp);
		
		return res;
	}
	
	@Override
	public Node visitVarExp(VarExpContext ctx) {
		//this corresponds to a variable access
		return new IdNode(ctx.ID().getText());
	}

    @Override
    public Node visitThisExp(FOOLParser.ThisExpContext ctx) {
        // TODO: implement
        ThisNode res = null;
        return res;
    }
	
	@Override
	public Node visitFunExp(FunExpContext ctx) {
		//this corresponds to a function invocation
		
		//declare the result
		Node res;
		
		//get the invocation arguments
		ArrayList<Node> args = new ArrayList<Node>();
		
		for(ExpContext exp : ctx.exp())
			args.add(visit(exp));
		
		//especial check for stdlib func
		//this is WRONG, THIS SHOULD BE DONE IN A DIFFERENT WAY
		//JUST IMAGINE THERE ARE 800 stdlib functions...
		if(ctx.ID().getText().equals("print")) {
            res = new PrintNode(args.get(0));
        } else {
            //instantiate the invocation
            res = new CallNode(ctx.ID().getText(), args);
        }
		return res;
	}

    @Override
    public Node visitMethodExp(FOOLParser.MethodExpContext ctx) {
        // TODO: implement
        MethodNode res = null;
        return res;
    }

    @Override
    public Node visitNewExp(FOOLParser.NewExpContext ctx) {
        // TODO: implement
        NewNode res = null;
        return res;
    }
	
}
