package ast.node;

import ast.SymbolTableEntry;
import ast.type.ArrowType;
import ast.type.Type;
import ast.type.TypeException;
import lib.FOOLlib;
import parser.FOOLParser;
import util.Environment;
import util.SemanticError;

import java.util.ArrayList;
import java.util.HashMap;

public class FunNode extends Node {

    private String id;
    private Type type;
    private ArrayList<INode> parlist = new ArrayList<INode>();
    private ArrayList<INode> declist;
    private INode body;

    public FunNode(FOOLParser.FunContext ctx, String i, Type t) {
        super(ctx);
        id = i;
        type = t;
    }

    public void addDecBody(ArrayList<INode> d, INode b) {
        declist = d;
        body = b;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        //create result list
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

        //env.offset = -2;
        HashMap<String, SymbolTableEntry> hm = env.symTable.get(env.nestingLevel);
        SymbolTableEntry entry = new SymbolTableEntry(env.nestingLevel, env.offset--); //separo introducendo "entry"

        if (hm.put(id, entry) != null)
            res.add(new SemanticError("Fun id " + id + " already declared"));
        else {
            //creare una nuova hashmap per la symTable
            env.nestingLevel++;
            HashMap<String, SymbolTableEntry> hmn = new HashMap<String, SymbolTableEntry>();
            env.symTable.add(hmn);

            ArrayList<Type> parTypes = new ArrayList<Type>();
            int paroffset = 1;

            //check args
            for (INode a : parlist) {
                ParameterNode arg = (ParameterNode) a;
                parTypes.add(arg.getType());
                if (hmn.put(arg.getId(), new SymbolTableEntry(env.nestingLevel, arg.getType(), paroffset++)) != null)
                    System.out.println("Parameter id " + arg.getId() + " already declared");
            }

            //set func type
            entry.addType(new ArrowType(parTypes, type));

            //check semantics in the dec list
            if (declist.size() > 0) {
                env.offset = -2;
                //if there are children then check semantics for every child and save the results
                for (INode n : declist)
                    res.addAll(n.checkSemantics(env));
            }

            //check body
            res.addAll(body.checkSemantics(env));

            //close scope
            env.symTable.remove(env.nestingLevel--);
        }
        return res;
    }

    @Override
    public Type type() throws TypeException {
        if (declist != null)
            for (INode dec : declist) {
                dec.type();
            }
        if (!(FOOLlib.isSubtype(body.type(), type))) {  // TODO: [Albi] Controllare che basti exp()
            throw new TypeException("Wrong return type for function: " + id, ctx);
        }
        // TODO: [Albi] secondo me anche le funzioni hanno un tipo
        return null;
    }

    public void addPar(INode p) {
        parlist.add(p);
    }

    public String codeGeneration() {

        StringBuilder declCode = new StringBuilder();
        if (declist != null)
            for (INode dec : declist)
                declCode.append(dec.codeGeneration());

        StringBuilder popDecl = new StringBuilder();
        if (declist != null)
            for (INode dec : declist)
                popDecl.append("pop\n");

        StringBuilder popParl = new StringBuilder();
        for (INode dec : parlist)
            popParl.append("pop\n");

        String funl = FOOLlib.freshFunLabel();
        FOOLlib.putCode(funl + ":\n" +
                "cfp\n" + //setta $fp a $sp
                "lra\n" + //inserimento return address
                declCode + //inserimento dichiarazioni locali
                body.codeGeneration() +
                "srv\n" + //pop del return value
                popDecl +
                "sra\n" + // pop del return address
                "pop\n" + // pop di AL
                popParl +
                "sfp\n" +  // setto $fp a valore del CL
                "lrv\n" + // risultato della funzione sullo stack
                "lra\n" + "js\n" // salta a $ra
        );

        return "push " + funl + "\n";
    }

    @Override
    public ArrayList<INode> getChilds() {
        ArrayList<INode> childs = new ArrayList<>();

        if(parlist != null && parlist.size()>0) {
            childs.addAll(parlist);
        }

        if(declist != null && declist.size()>0) {
            childs.addAll(declist);
        }

        childs.add(body);

        return childs;
    }

    @Override
    public String toString() {
        return "Fun -> " + id + ": " + type;
    }

}  