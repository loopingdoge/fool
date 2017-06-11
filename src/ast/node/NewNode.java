package ast.node;

import ast.SymbolTableEntry;
import ast.type.InstanceType;
import ast.type.Type;
import ast.type.TypeException;
import parser.FOOLParser;
import util.Environment;
import util.SemanticError;

import java.util.ArrayList;

public class NewNode extends Node {

    private String id;
    private ArrayList<INode> params;
    private SymbolTableEntry entry;
    private int nestinglevel;

    public NewNode(FOOLParser.NewExpContext ctx, String id, ArrayList<INode> params) {
        super(ctx);
        this.id = id;
        this.params = params;
    }

    @Override
    public Type type() throws TypeException {
        for (INode param : params)
            param.type();

        if (entry.getType() instanceof InstanceType) {
            throw new TypeException("Wrong usage of new operator", ctx);
        }

        return new InstanceType();//entry.getType();
    }

    @Override
    public String codeGeneration() {
        return "New node not implemented yet";
    }

    @Override
    public ArrayList<INode> getChilds() {
        return new ArrayList<>();
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        //create the result
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

        if (params.size() > 0) {
            //if there are children then check semantics for every child and save the results
            for (INode n : params)
                res.addAll(n.checkSemantics(env));
        }

        int j = env.nestingLevel;
        SymbolTableEntry tmp = null;
        while ( j >= 0 && tmp == null)
            tmp=(env.symTable.get(j--)).get(id);
        if (tmp==null)
            res.add(new SemanticError("Id "+id+" not declared"));
        else {
            entry = tmp;
            nestinglevel = env.nestingLevel;
        }

        return res;
    }

    @Override
    public String toString() {
        return "new";
    }

}
