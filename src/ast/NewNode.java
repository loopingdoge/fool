package ast;

import util.Environment;
import util.SemanticError;

import java.util.ArrayList;

public class NewNode implements Node {

    private String id;
    private ArrayList<Node> explist;
    SymbolTableEntry entry;
    int nestinglevel;

    public NewNode (String i, ArrayList<Node> e) {
        id = i;
        explist = e;
    }

    @Override
    public String toPrint(String indent) {
        String explstr="";
        for (Node exp:explist)
            explstr += exp.toPrint(indent + "  " );
        return indent+"New:" + id + "\n" + explstr ;
    }

    @Override
    public Type typeCheck() {
        for (Node exp:explist)
            exp.typeCheck();

        if (entry.getType() instanceof InstanceType) {
            System.out.println("Wrong usage of function identifier");
            System.exit(0);
        }

        return entry.getType();
    }

    @Override
    public String codeGeneration() {
        // TODO: implement
        return null;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        //create the result
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

        if (explist.size() > 0) {
            //if there are children then check semantics for every child and save the results
            for(Node n : explist)
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
}
