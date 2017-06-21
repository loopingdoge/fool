package node;

import symbol_table.SymbolTableEntry;
import type.ClassType;
import type.Type;
import type.TypeException;
import type.TypeID;
import org.antlr.v4.runtime.ParserRuleContext;
import grammar.FOOLParser;
import symbol_table.Environment;
import symbol_table.RedeclaredVarException;
import main.SemanticError;
import symbol_table.UndeclaredVarException;

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

    public ClassNode(ParserRuleContext ctx, String classID) {
        super(ctx);
        this.classID = classID;
    }

    public ClassNode(ParserRuleContext ctx, String classID, ArrayList<MethodNode> fundeclist) {
        super(ctx);
        this.classID = classID;
        this.fundeclist = fundeclist;
    }

    public ClassNode(ParserRuleContext ctx, String classID, ArrayList<VarNode> vardeclist, ArrayList<MethodNode> fundeclist) {
        super(ctx);
        this.classID = classID;
        this.vardeclist = vardeclist;
        this.fundeclist = fundeclist;
    }

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
            env.addEntry(classID, new ClassType(classID, superClassID, fields, methods), 0);
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


        if(superClassID != null && !superClassID.isEmpty()) {
            try {
                SymbolTableEntry superClassEntry = env.getLatestEntryOf(superClassID);
                ClassType superClassType = (ClassType) superClassEntry.getType();

                HashMap<String, Type> superClassFields = superClassType.getFields();
                for(String localField : fields.keySet()){
                    if(superClassFields.containsKey(localField)){
                        if(!superClassFields.get(localField).isSubTypeOf(fields.get(localField))) {
                            res.add(new SemanticError("Field '" + localField + "'  overrided from super class with different type."));
                        }
                    }
                }

                HashMap<String, Type> superClassMethods = superClassType.getMethods();
                for(String localMethod : methods.keySet()){
                    if(superClassMethods.containsKey(localMethod)){
                        if(!superClassMethods.get(localMethod).isSubTypeOf(methods.get(localMethod))){
                            res.add(new SemanticError("Method '" + localMethod + "'  overrided from super class with different type."));
                        }
                    }
                }

            } catch (UndeclaredVarException ex) {
                res.add(new SemanticError("Super class " + superClassID + " not defined. " + ex.getMessage()));
            }
        }

        return res;
    }

    @Override
    public Type type() throws TypeException {

        return new ClassType("TODO: ...");
    }

    @Override
    public String codeGeneration() {
        // TODO: implement
        return null;
    }

    @Override
    public ArrayList<INode> getChilds() {
        ArrayList<INode> res = new ArrayList<INode>();
        res.addAll(vardeclist);
        res.addAll(fundeclist);
        return res;
    }

    @Override
    public String toString() {
        return "class " + classID + " extends " + superClassID;
    }

}
