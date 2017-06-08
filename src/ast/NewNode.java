package ast;

import util.Environment;
import util.SemanticError;

import java.util.ArrayList;

public class NewNode implements Node {

    private String id;
    private ArrayList<Node> params;
    SymbolTableEntry entry;
    int nestinglevel;

    public NewNode (String id, ArrayList<Node> params) {
        this.id = id;
        this.params = params;
    }

    @Override
    public String toPrint(String indent) {
        String paramsToString = params.stream()
                .map(param -> param.toPrint(indent + "  "))
                .reduce("", String::concat);
        return indent + "New: " + id  + "\n"
                + entry.toPrint(indent + "  ")
                + paramsToString;
    }

    @Override
    public Type typeCheck() {

        for (Node exp:params)
            exp.typeCheck();

        if (entry.getType() instanceof InstanceType) {
            System.out.println("Wrong usage of new operator");
            System.exit(0);
        }

        return new InstanceType();//entry.getType();
    }

    @Override
    public String codeGeneration() {
        return "New node to be implemented yet";
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        //create the result
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

        if (params.size() > 0) {
            //if there are children then check semantics for every child and save the results
            for(Node n : params)
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
