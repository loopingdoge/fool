package ast;

import util.Environment;
import util.SemanticError;

import java.util.ArrayList;
import java.util.HashMap;

public class ProgClassDecNode implements Node {

    private Type type;
    private String classID;
    private String superClassID;
    private ArrayList<Node> vardeclist;
    private ArrayList<Node> fundeclist;
    SymbolTableEntry stEntry;
    int nestinglevel = 0;

    public ProgClassDecNode(Type type, String classID, String superClassID, ArrayList<Node> vardeclist, ArrayList<Node> fundeclist) {
        this.type = type;
        this.classID = classID;
        this.superClassID = superClassID;
        this.vardeclist = vardeclist;
        this.fundeclist = fundeclist;
    }

    @Override
    public String toPrint(String indent) {

        String vardecToString = vardeclist.stream()
                .map(vardeclist -> vardeclist.toPrint(indent + "  "))
                .reduce("", String::concat);

        String fundecToString = fundeclist.stream()
                .map(fundeclist -> fundeclist.toPrint(indent + "  "))
                .reduce("", String::concat);

        return indent + "Class declarated " + classID + "\n"
                + indent + "extends: " + superClassID + "\n"
                + indent + stEntry.toPrint(indent + "  ")
                + indent + "fields: " + vardecToString
                + indent + "methods: " + fundecToString;
    }

    @Override
    public Type typeCheck() {

        // TODO: check for duplicate variables (superclass)
        for (Node dec : vardeclist)
            dec.typeCheck();

        // TODO: correct method overriding check
        for (Node dec : fundeclist)
            dec.typeCheck();

        return type;
    }

    @Override
    public String codeGeneration() {
        // TODO: implement
        return null;
    }

    @Override
    public ArrayList<Node> getChilds() {
        return null;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {

        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

        if (vardeclist.size() > 0) {
            for(Node n : vardeclist)
                res.addAll(n.checkSemantics(env));
        }

        if (fundeclist.size() > 0) {
            for(Node n : fundeclist)
                res.addAll(n.checkSemantics(env));
        }

        // check if super class id exists
        if (superClassID != null && !superClassID.isEmpty()) {

            int j = env.nestingLevel;
            SymbolTableEntry tmp = null;
            while ( j >= 0 && tmp == null)
                tmp = (env.symTable.get(j--)).get(superClassID);
            if (tmp == null)
                res.add(new SemanticError("Id of the super class: " + superClassID + " not declared"));
            else {  // non necessario?
                stEntry = tmp;
                nestinglevel = env.nestingLevel;
            }
        }

        HashMap<String, SymbolTableEntry> hm = env.symTable.get(0);
        SymbolTableEntry entry = new SymbolTableEntry(0, type, env.offset);

        // inserisco la nuova classe nella symbol table
        if (hm.put(classID, entry) != null)
            res.add(new SemanticError("Class with id:" + classID + " already declared!"));

        return res;
    }
}
