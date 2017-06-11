package ast.node;

import ast.SymbolTableEntry;
import ast.type.ClassType;
import ast.type.Type;
import ast.type.TypeException;
import parser.FOOLParser;
import util.Environment;
import util.SemanticError;

import java.util.ArrayList;
import java.util.HashMap;

public class ClassNode extends Node {

    private Type type;
    private String classID;
    private String superClassID;
    private ArrayList<Node> vardeclist;
    private ArrayList<Node> fundeclist;
    private SymbolTableEntry stEntry;
    private int nestinglevel = 0;

    public ClassNode(FOOLParser.ClassdecContext ctx) {
        super(ctx);
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {

        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

        System.out.println("[DEBUG] ClassNode.checkSemantics()  env.nestingLevel = " + env.nestingLevel);
        HashMap<String, SymbolTableEntry> hm = env.symTable.get(env.nestingLevel);
        SymbolTableEntry entry = new SymbolTableEntry(nestinglevel, env.offset);

        // inserisco nuovo ID classe nella lista di symbol table
        if (hm.put(classID, entry) != null)
            res.add(new SemanticError("Class with id:" + classID + " already declared!"));


        //creare una nuova hashmap per la classe symTable
        // env.nestingLevel++;
        HashMap<String, SymbolTableEntry> hmn = new HashMap<String, SymbolTableEntry>();
        env.symTable.add(hmn);

        ArrayList<Type> fieldsTypes = new ArrayList<Type>();
        int fieldsOffset = 1;

        //check (fields) var declarations
        for (Node a : vardeclist) {
            VarNode var = (VarNode) a;
            fieldsTypes.add(var.getType());
            if (hmn.put(var.getId(), new SymbolTableEntry(env.nestingLevel, var.getType(), fieldsOffset++)) != null)
                System.out.println("Fields with id " + var.getId() + " already declared");
        }

        ArrayList<Type> methodsTypes = new ArrayList<Type>();
        int methodsOffset = 1;

        //check (fields) var declarations
        for (Node a : fundeclist) {
            MethodNode method = (MethodNode) a;
            methodsTypes.add(method.getType());
            if (hmn.put(method.getId(), new SymbolTableEntry(env.nestingLevel, method.getType(), fieldsOffset++)) != null)
                System.out.println("Method with id " + method.getId() + " already declared");
        }

        //set class type in st entry
        entry.addType(new ClassType(classID, superClassID, fieldsTypes, methodsTypes));


        // check if super class id exists
        if (superClassID != null && !superClassID.isEmpty()) {
            // TODO: implent inheritance
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
        } else {




        }

        if (vardeclist.size() > 0) {
            for(Node n : vardeclist)
                res.addAll(n.checkSemantics(env));
        }

        if (fundeclist.size() > 0) {
            for(Node n : fundeclist)
                res.addAll(n.checkSemantics(env));
        }

        return res;
    }

    @Override
    public Type type() throws TypeException {

        // TODO: check for duplicate variables (superclass)
        for (Node dec : vardeclist)
            dec.type();

        // TODO: correct method overriding check
        for (Node dec : fundeclist)
            dec.type();

        return type;
    }

    @Override
    public String codeGeneration() {
        // TODO: implement
        return null;
    }

    @Override
    public ArrayList<INode> getChilds() {
        return null;
    }

    @Override
    public String toString() {
        return "class " + classID + " extends " + superClassID;
    }

}
