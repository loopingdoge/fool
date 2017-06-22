package node;

import exception.UndeclaredVarException;
import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import exception.RedeclaredVarException;
import type.ClassType;
import type.Type;
import exception.TypeException;

import java.util.ArrayList;

public class VarNode extends Node {

    private String id;
    private Type type;   // TODO: [Pietro] quando viene istanziato un oggetto rimane null e crasha tutto nel type checking
    private INode exp;

    private String className;

    public VarNode(FOOLParser.VarasmContext ctx, String i, Type t, INode v) {
        super(ctx);
        id = i;
        type = t;
        exp = v;
        //SPORCHISSIMA MA FUNZIONA
        className = ctx.children.get(0).getChild(0).getText();
    }

    public Type getType() {
        return this.type;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        //create result list
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

        //Se è un ID controllo che esista come tipo di classe precedentemente dichiarata
        if (this.type == null) {
            try {
                this.type = env.getTypeOf(className);
            } catch (UndeclaredVarException e) {
                res.add(new SemanticError(e.getMessage()));
            }
        }

        //env.offset = -2;
        try {
            env.addEntry(id, this.type, env.offset--);
        } catch (RedeclaredVarException e) {
            res.add(new SemanticError(e.getMessage()));
        }

        res.addAll(exp.checkSemantics(env));

        return res;
    }

    @Override
    public Type type() throws TypeException {
        FOOLParser.VarasmContext varasmContext = (FOOLParser.VarasmContext) ctx;
        if (!exp.type().isSubTypeOf(type)) {
            throw new TypeException("incompatible value for variable " + id, varasmContext.exp());
        }

        return this.type;
    }

    @Override
    public String codeGeneration() {
        return exp.codeGeneration();
    }

    @Override
    public ArrayList<INode> getChilds() {
        ArrayList<INode> childs = new ArrayList<>();
        childs.add(exp);
        return childs;
    }

    @Override
    public String toString(){
        return id + ": " + type;
    }

}  