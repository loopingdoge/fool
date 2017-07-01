package main;

import exception.TypeException;
import grammar.FOOLBaseVisitor;
import grammar.FOOLLexer;
import grammar.FOOLParser;
import grammar.FOOLParser.*;
import node.*;
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
        res = new ProgLetInNode(ctx, new LetNode(ctx.let(), declarations), new InNode(ctx.let(), exp, true));

        return res;
    }

    @Override
    public INode visitClassExp(FOOLParser.ClassExpContext ctx) {
        ProgClassDecNode res;

        try {
            ArrayList<ClassNode> classDeclarations = new ArrayList<ClassNode>();
            for (FOOLParser.ClassdecContext dc : ctx.classdec()) {
                ArrayList<ParameterNode> vars = new ArrayList<ParameterNode>();
                for (int i = 0; i < dc.vardec().size(); i++) {
                    VardecContext varctx = dc.vardec().get(i);
                    vars.add(new ParameterNode(varctx, varctx.ID().getText(), visit(varctx.type()).type(), i + 1, true));
                }
                ArrayList<MethodNode> mets = new ArrayList<MethodNode>();
                for (MetContext functx : dc.met()) {
                    MethodNode method = (MethodNode) visit(functx);
                    method.setClassID(dc.ID(0).getText());
                    mets.add(method);
                }

                ClassNode classNode;
                if (dc.ID(1) == null) {
                    classNode = new ClassNode(dc, dc.ID(0).getText(), "", vars, mets);
                } else {
                    classNode = new ClassNode(dc, dc.ID(0).getText(), dc.ID().get(1).getText(), vars, mets);
                }
                classDeclarations.add(classNode);
            }

            if (ctx.let() != null) {
                ArrayList<INode> letDeclarations = new ArrayList<INode>();
                for (DecContext dc : ctx.let().dec()) {
                    letDeclarations.add(visit(dc));
                }

                System.out.println("[DEBUG] FoolVisitorImpl.visitClassExp() found " + classDeclarations.size() + " classes, and " + letDeclarations.size() + " let declarations");

                INode exp = visit(ctx.exp());

                res = new ProgClassDecNode(ctx, classDeclarations, new LetNode(ctx.let(), letDeclarations), new InNode(ctx.let(), exp, true));
            } else {
                INode exp = visit(ctx.exp());

                res = new ProgClassDecNode(ctx, classDeclarations, null, new InNode(ctx.let(), exp, false));
            }
        } catch (TypeException e) {
            return new ErrorNode(e);
        }


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
            for (int i = 0; i < ctx.vardec().size(); i++) {
                VardecContext vc = ctx.vardec().get(i);
                params.add(new ParameterNode(vc, vc.ID().getText(), visit(vc.type()).type(), i + 1));
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
    public INode visitMet(FOOLParser.MetContext ctx) {
        try {
            // Get FunContext since Meth is just Fun (Walter White quote)
            FunContext fctx = ctx.fun();
            // initialize @res with the visits to the type and its ID
            ArrayList<ParameterNode> params = new ArrayList<>();

            // add argument declarations
            // we are getting a shortcut here by constructing directly the ParameterNode
            // this could be done differently by visiting instead the VardecContext
            for (int i = 0; i < fctx.vardec().size(); i++) {
                VardecContext vc = fctx.vardec().get(i);
                params.add(new ParameterNode(vc, vc.ID().getText(), visit(vc.type()).type(), i + 1));
            }

            // add body, create a list for the nested declarations
            ArrayList<INode> declarations = new ArrayList<INode>();
            // check whether there are actually nested decs
            if (fctx.let() != null) {
                // if there are visit each dec and add it to the @innerDec list
                for (DecContext dc : fctx.let().dec())
                    declarations.add(visit(dc));
            }

            // get the exp body
            INode body = visit(fctx.exp());

            return new MethodNode(fctx, fctx.ID().getText(), visit(fctx.type()).type(), params, declarations, body);
        } catch (TypeException e) {
            return new ErrorNode(e);
        }
    }

    @Override
    public INode visitType(TypeContext ctx) {
        return new TypeNode(ctx, ctx.getText());
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
                return new TimesNode(ctx, visit(ctx.left), visit(ctx.right));
            } else {
                return new DivNode(ctx, visit(ctx.left), visit(ctx.right));
            }
        }
    }

    @Override
    public INode visitFactor(FactorContext ctx) {
        //check whether this is a simple or binary expression
        //notice here the necessity of having named elements in the grammar
        try {
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
                    case FOOLLexer.GREATER:
                        return new GreaterNode(ctx, visit(ctx.left), visit(ctx.right));
                    case FOOLLexer.LESS:
                        return new LessNode(ctx, visit(ctx.left), visit(ctx.right));
                    case FOOLLexer.OR:
                        return new OrNode(ctx, visit(ctx.left), visit(ctx.right));
                    default:
                        throw new Exception("Invalid operator");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
            return new BoolNode(ctx, Boolean.parseBoolean(text));
        } else {
            return new NotNode(ctx, new BoolNode(ctx, Boolean.parseBoolean(text)));
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
        return new ThisNode(ctx);
    }

    @Override
    public INode visitFunExp(FunExpContext ctx) {
        return visit(ctx.funcall());
    }

    @Override
    public INode visitMethodExp(FOOLParser.MethodExpContext ctx) {
        ArrayList<INode> args = new ArrayList<>();
        for (ExpContext exp : ctx.funcall().exp()) {
            args.add(visit(exp));
        }

        String methodId = ctx.funcall().ID().getText();
        String objectId = ctx.THIS() != null ?
                ctx.THIS().getText()
                :
                ctx.ID().getText();
        return new MethodCallNode(ctx, objectId, methodId, new ArgumentsNode(ctx, args));
    }

    @Override
    public INode visitFuncall(FOOLParser.FuncallContext ctx) {
        //this corresponds to a function invocation

        //declare the result
        INode res;

        //get the invocation arguments
        ArrayList<INode> args = new ArrayList<INode>();

        for (ExpContext exp : ctx.exp())
            args.add(visit(exp));

        String funcId = ctx.ID().getText();

        res = new FunCallNode(ctx, funcId, new ArgumentsNode(ctx, args));

        return res;
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

    @Override
    public INode visitPrint(FOOLParser.PrintContext ctx) {
        //declare the result
        INode res;

        //get the invocation arguments
        ArrayList<INode> args = new ArrayList<INode>();

        res = new PrintNode(ctx, visit(ctx.exp()));

        return res;
    }

}
