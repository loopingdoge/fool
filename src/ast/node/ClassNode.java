package ast.node;

import ast.SymbolTableEntry;
import ast.type.ClassType;
import ast.type.Type;
import ast.type.TypeException;
import ast.type.TypeID;
import org.antlr.v4.runtime.ParserRuleContext;
import parser.FOOLParser;
import util.Environment;
import util.RedeclaredVarException;
import util.SemanticError;
import util.UndeclaredVarException;

import java.util.ArrayList;
import java.util.HashMap;

public class ClassNode extends Node {


    private String classID;
    private String superClassID;
    private ArrayList<VarNode> vardeclist;
    private ArrayList<MethodNode> fundeclist;

    private HashMap<String, Type> fields = new HashMap<>();
    private HashMap<String, Type> methods = new HashMap<>();

    private SymbolTableEntry stEntry;
    private int nestinglevel = 0;

    public ClassNode(ParserRuleContext ctx, String classID, String superClassID, ArrayList<VarNode> vardeclist, ArrayList<MethodNode> fundeclist) {
        super(ctx);
        this.classID = classID;
        this.superClassID = superClassID;
        this.vardeclist = vardeclist;
        this.fundeclist = fundeclist;
    }


    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {

        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

        for (VarNode var : vardeclist) {
            fields.put(var.getId(), var.getType());
        }
        for (MethodNode fun : fundeclist) {
            methods.put(fun.getId(), fun.getType());
        }

        try {
            env.addEntry(classID, new SymbolTableEntry(env.getNestingLevel() + 1, new ClassType(classID, superClassID, fields, methods), 0));
        } catch (RedeclaredVarException ex) {
            res.add(new SemanticError(ex.getMessage()));
        }


        for (VarNode var : vardeclist) {
            res.addAll(var.checkSemantics(env));
        }
        for (MethodNode fun : fundeclist) {
            res.addAll(fun.checkSemantics(env));
        }

        try {
            if (!(env.getTypeOf(superClassID) instanceof ClassType))
                res.add(new SemanticError("ID of super class " + superClassID + " is not related to a class type"));
        } catch (UndeclaredVarException exp) {
            res.add(new SemanticError("Super class " + superClassID + "not defined"));
        }

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
        for (INode a : vardeclist) {
            VarNode var = (VarNode) a;
            fieldsTypes.add(var.getType());
            if (hmn.put(var.getId(), new SymbolTableEntry(env.nestingLevel, var.getType(), fieldsOffset++)) != null)
                System.out.println("Fields with id " + var.getId() + " already declared");
        }

        ArrayList<Type> methodsTypes = new ArrayList<Type>();
        int methodsOffset = 1;

        //check (fields) var declarations
        for (INode a : fundeclist) {
            MethodNode method = (MethodNode) a;
            methodsTypes.add(method.getType());
            if (hmn.put(method.getId(), new SymbolTableEntry(env.nestingLevel, method.getType(), fieldsOffset++)) != null)
                System.out.println("Method with id " + method.getId() + " already declared");
        }

        //set class type in st entry
        // TODO: [Albi] ho commentato perche' dava errore, passare i parametri giusti
        // entry.addType(new ClassType(classID, superClassID, fieldsTypes, methodsTypes));

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
            for(INode n : vardeclist)
                res.addAll(n.checkSemantics(env));
        }

        if (fundeclist.size() > 0) {
            for(INode n : fundeclist)
                res.addAll(n.checkSemantics(env));
        }

        return res;
    }

    @Override
    public Type type() throws TypeException {

        // TODO: check for duplicate variables (superclass)
        for (INode dec : vardeclist)
            dec.type();

        // TODO: correct method overriding check
        for (INode dec : fundeclist)
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
