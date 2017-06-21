package node;

import exception.UndeclaredClassException;
import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import symbol_table.SymbolTableEntry;
import exception.UndeclaredVarException;
import type.Type;
import type.ClassType;
import type.InstanceType;
import util.CodegenUtils;

import java.util.ArrayList;

public class NewNode extends Node {

    private String classID;
    private ClassType classT;
    private ArrayList<INode> params;
    private SymbolTableEntry entry;
    private int nestinglevel;

    public NewNode(FOOLParser.NewExpContext ctx, String classID, ArrayList<INode> params) {
        super(ctx);
        this.classID = classID;
        this.params = params;
    }

    @Override
    public Type type() {
        return new InstanceType( classT );
    }

    @Override
    public String codeGeneration() {
        return "New node not implemented yet";
    }

    @Override
    public ArrayList<INode> getChilds() {
        ArrayList<INode> res = new ArrayList<INode>();

        res.addAll(params);

        return res;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        //create the result
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

        try {
            classT = CodegenUtils.getClassEntry( classID );
        } catch (UndeclaredClassException e) {
            res.add(new SemanticError( e.getMessage() ));
        }

        if (params.size() > 0) {
            //if there are children then check semantics for every child and save the results
            for (INode n : params)
                res.addAll(n.checkSemantics(env));
        }

        try {
            this.entry = env.getLatestEntryOf(this.classID);
            this.nestinglevel = env.getNestingLevel();
        } catch (UndeclaredVarException e) {
            res.add(new SemanticError(e.getMessage()));
        }

        return res;
    }

    @Override
    public String toString() {
        return "new " + classID ;
    }

}
