package node;

import exception.TypeException;
import grammar.FOOLBaseVisitor;
import grammar.FOOLLexer;
import grammar.FOOLParser;
import grammar.FOOLParser.*;
import type.Type;

import java.util.ArrayList;

public class FoolVisitorImpl extends FOOLBaseVisitor<INode> {

    @Override
    public INode visitSingleExp(SingleExpContext ctx) {
        return new ProgSingleExpNode(ctx, visit(ctx.exp()));
    }

    @Override
    public INode visitLetInExp(LetInExpContext ctx) {
        //resulting node of the right type

        ProgLetInNode res;

        //list of declarations in @res
        ArrayList<INode> declarations = new ArrayList<INode>();

        //visit all nodes corresponding to declarations inside the let context and store them in @declarations
        //notice that the ctx.let().dec() method returns a list, this is because of the use of * or + in the grammar
        //antlr detects this is a group and therefore returns a list
        for (DecContext dc : ctx.let().dec()) {
            declarations.add(visit(dc));
        }

        //visit exp context
        INode exp = visit(ctx.exp());

        //build @res accordingly with the result of the visits to its content
        res = new ProgLetInNode(ctx, declarations, exp);

        return res;
    }

    @Override
    public INode visitClassExp(FOOLParser.ClassExpContext ctx) {
        ProgClassDecNode res;

        ArrayList<ClassNode> classDeclarations = new ArrayList<ClassNode>();
        for (FOOLParser.ClassdecContext dc : ctx.classdec()) {
            ArrayList<VarNode> vars = new ArrayList<VarNode>();
            for (VardecContext varctx: dc.vardec()) {
               vars.add((VarNode) visit(varctx));
            }
            ArrayList<FunNode> funs = new ArrayList<FunNode>();
            for (VardecContext functx: dc.vardec()) {
                funs.add((FunNode) visit(functx));
            }
            ClassNode classNode = new ClassNode(dc, dc.ID(0).getText(), dc.ID(1).getText(), vars, funs);
            classDeclarations.add(classNode);
        }

        ArrayList<INode> letDeclarations = new ArrayList<INode>();
        for (DecContext dc : ctx.let().dec()) {
            letDeclarations.add(visit(dc));
        }

        System.err.println("[DEBUG] FoolVisitorImpl.visitClassExp() found " + classDeclarations.size() + " classes, and " + letDeclarations.size() + " let declarations");

        INode exp = visit(ctx.exp());

        res = new ProgClassDecNode(ctx, classDeclarations, letDeclarations, exp);

        return res;
    }

    @Override
    public INode visitVarasm(VarasmContext ctx) {
        //declare the result node
        VarNode result;

        //visit the type
        Type typeNode = null;
        try {
            typeNode = visit(ctx.vardec().type()).type();
        } catch (TypeException e) {
            return new ErrorNode(e);
        }

        //visit the exp
        INode expNode = visit(ctx.exp());

        //build the varNode
        return new VarNode(ctx, ctx.vardec().ID().getText(), typeNode, expNode);
    }

    @Override
    public INode visitFun(FunContext ctx) {
        try {
            // initialize @res with the visits to the type and its ID
            ArrayList<ParameterNode> params = new ArrayList<>();

            // add argument declarations
            // we are getting a shortcut here by constructing directly the ParameterNode
            // this could be done differently by visiting instead the VardecContext
            for (VardecContext vc : ctx.vardec()) {
                params.add(new ParameterNode(vc, vc.ID().getText(), visit(vc.type()).type()));
            }

            // add body, create a list for the nested declarations
            ArrayList<INode> declarations = new ArrayList<INode>();
            // check whether there are actually nested decs
            if (ctx.let() != null) {
                // if there are visit each dec and add it to the @innerDec list
                for (DecContext dc : ctx.let().dec())
                    declarations.add(visit(dc));
            }

            // get the exp body
            INode body = visit(ctx.exp());

            return new FunNode(ctx, ctx.ID().getText(), visit(ctx.type()).type(), params, declarations, body);
        } catch (TypeException e) {
            return new ErrorNode(e);
        }
    }

    @Override
    public INode visitType(TypeContext ctx) {
//        if (ctx.getText().equals("int"))
//            return new IntNode(ctx, 0);
//        else if (ctx.getText().equals("bool"))
//            return new BoolNode(ctx, true, false);
        return new TypeNode(ctx, ctx.getText());
        // TODO: Controllare anche arrow ed instance types
        //this will never happen thanks to the parser
    }

    @Override
    public INode visitExp(ExpContext ctx) {
        //this could be enhanced

        //check whether this is a simple or binary expression
        //notice here the necessity of having named elements in the grammar
        if (ctx.right == null) {
            //it is a simple expression
            return visit(ctx.left);
        } else {
            //it is a binary expression, you should visit left and right
            if (ctx.operator.getType() == FOOLLexer.PLUS) {
                return new PlusNode(ctx, visit(ctx.left), visit(ctx.right));
            } else {
                return new MinusNode(ctx, visit(ctx.left), visit(ctx.right));
            }
        }
    }

    @Override
    public INode visitTerm(TermContext ctx) {
        //check whether this is a simple or binary expression
        //notice here the necessity of having named elements in the grammar
        if (ctx.right == null) {
            //it is a simple expression
            return visit(ctx.left);
        } else {
            //it is a binary expression, you should visit left and right
            if (ctx.operator.getType() == FOOLLexer.TIMES) {
                return new MultNode(ctx, visit(ctx.left), visit(ctx.right));
            } else {
                return new DivNode(ctx, visit(ctx.left), visit(ctx.right));
            }
        }
    }


    @Override
    public INode visitFactor(FactorContext ctx) {
        //check whether this is a simple or binary expression
        //notice here the necessity of having named elements in the grammar
        if (ctx.right == null) {
            //it is a simple expression
            return visit(ctx.left);
        } else {
            //it is a binary expression, you should visit left and right
            switch (ctx.operator.getType()) {
                case FOOLLexer.EQ:
                    return new EqualNode(ctx, visit(ctx.left), visit(ctx.right));
                case FOOLLexer.LEQ:
                    return new LessEqualNode(ctx, visit(ctx.left), visit(ctx.right));
                case FOOLLexer.GEQ:
                    return new GreaterEqualNode(ctx, visit(ctx.left), visit(ctx.right));
                case FOOLLexer.AND:
                    return new AndNode(ctx, visit(ctx.left), visit(ctx.right));
                default:
                    return new OrNode(ctx, visit(ctx.left), visit(ctx.right));
            }
        }
    }

    @Override
    public INode visitIntVal(IntValContext ctx) {
        // notice that this method is not actually a rule but a named production #intVal

        //there is no need to perform a check here, the lexer ensures this text is an int
        return new IntNode(ctx, Integer.parseInt(ctx.INTEGER().getText()));
    }

    @Override
    public INode visitBoolVal(BoolValContext ctx) {
        //there is no need to perform a check here, the lexer ensures this text is a boolean
        //[ADDED] With NOT operator Boolean.parseBoolean accept i.e. '!4' with a 'false' value, the correct solution is to check the input.
        String text = ctx.getText().replace("!", "");
        if (ctx.NOT() == null) {
            return new BoolNode(ctx, Boolean.parseBoolean(text), false);
        } else {
            return new BoolNode(ctx, Boolean.parseBoolean(text), true);
        }
    }

    @Override
    public INode visitBaseExp(BaseExpContext ctx) {

        //this is actually nothing in the sense that for the ast the parenthesis are not relevant
        //the thing is that the structure of the ast will ensure the operational order by giving
        //a larger depth (closer to the leafs) to those expressions with higher importance

        //this is actually the default implementation for this method in the FOOLBaseVisitor class
        //therefore it can be safely removed here

        return visit(ctx.exp());

    }

    @Override
    public INode visitIfExp(IfExpContext ctx) {

        //create the resulting node
        IfNode res;

        //visit the conditional, then the then branch, and then the else branch
        //notice once again the need of named terminals in the rule, this is because
        //we need to point to the right expression among the 3 possible ones in the rule

        INode condExp = visit(ctx.cond);

        INode thenExp = visit(ctx.thenBranch);

        INode elseExp = visit(ctx.elseBranch);

        //build the @res properly and return it
        res = new IfNode(ctx, condExp, thenExp, elseExp);

        return res;
    }

    @Override
    public INode visitVarExp(VarExpContext ctx) {
        //this corresponds to a variable access
        return new IdNode(ctx, ctx.ID().getText());
    }

    @Override
    public INode visitThisExp(FOOLParser.ThisExpContext ctx) {
        // TODO: implement
        ThisNode res = null;
        return res;
    }

    @Override
    public INode visitFunExp(FunExpContext ctx) {
        //this corresponds to a function invocation

        //declare the result
        INode res;

        //get the invocation arguments
        ArrayList<INode> args = new ArrayList<INode>();

        for (ExpContext exp : ctx.exp())
            args.add(visit(exp));

        //especial check for stdlib func
        //this is WRONG, THIS SHOULD BE DONE IN A DIFFERENT WAY
        //JUST IMAGINE THERE ARE 800 stdlib functions...
        if (ctx.ID().getText().equals("print")) {
            res = new PrintNode(ctx, args.get(0));
        } else {
            //instantiate the invocation
            res = new CallNode(ctx, ctx.ID().getText(), args);
        }

        return res;
    }

    @Override
    public INode visitMethodExp(FOOLParser.MethodExpContext ctx) {
        ArrayList<ParameterNode> params = new ArrayList<>();
        for (ExpContext exp : ctx.exp()) {
            params.add((ParameterNode) visit(exp));
        }

        String methodId = ctx.ID(ctx.ID().size() - 1).getText();
        String objectId = ctx.THIS() != null ?
                ctx.THIS().getText()
                :
                ctx.ID(0).getText();
        return new MethodCallNode(ctx, objectId, methodId, params);
    }

    @Override
    public INode visitNewExp(FOOLParser.NewExpContext ctx) {

        NewNode res;
        String id;
        ArrayList<INode> declarations = new ArrayList<INode>();

        id = ctx.ID().getText(); // prendo l'id

        for (ExpContext exp : ctx.exp()) {
            declarations.add(visit(exp)); // prendo tutti i parametri
        }

        res = new NewNode(ctx, id, declarations);

        return res;
    }

}
