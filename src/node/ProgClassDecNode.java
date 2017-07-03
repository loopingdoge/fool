package node;

import exception.RedeclaredVarException;
import main.SemanticError;
import org.antlr.v4.runtime.ParserRuleContext;
import symbol_table.Environment;
import type.ClassType;
import type.FunType;
import type.Type;
import exception.TypeException;
import util.Field;
import util.Method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

public class ProgClassDecNode extends Node {

    private ArrayList<ClassNode> classDeclarations;
    private LetNode let;
    private InNode in;

    public ProgClassDecNode(ParserRuleContext ctx, ArrayList<ClassNode> classDeclarations, LetNode l, InNode i) {
        super(ctx);
        this.classDeclarations = classDeclarations;
        this.let = l;
        this.in = i;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        ArrayList<SemanticError> res = new ArrayList<SemanticError>();

        env.pushHashMap();

        // preliminary class inserting in symbol table for order independent references

        for (ClassNode classNode : classDeclarations) {
            try {
                ArrayList<Field> fields = new ArrayList<Field>();
                for (ParameterNode field : classNode.getVardeclist()) {
                    fields.add(new Field(field.getID(), field.getType()));
                }
                ArrayList<Method> methods = new ArrayList<Method>();
                for (MethodNode method : classNode.getMetDecList()) {
                    ArrayList<Type> paramsType = new ArrayList<>();
                    for (ParameterNode parameter : method.getParams()) {
                        paramsType.add(parameter.getType());
                    }
                    methods.add(new Method(method.getId(), new FunType(paramsType, method.getDeclaredReturnType())));
                }
                ClassType classType = new ClassType(classNode.getClassID(), new ClassType(classNode.getSuperClassID()), fields, methods);
                env.addEntry(classNode.getClassID(), classType, 0);
            } catch (RedeclaredVarException e) {
                res.add(new SemanticError("Class '" + classNode.getClassID() + "' declared multiple times"));
            }
        }

        for (ClassNode classNode : classDeclarations) {
            res.addAll(classNode.checkSemantics(env));
        }

        if (let != null)
            res.addAll(let.checkSemantics(env));

        res.addAll(in.checkSemantics(env));

        env.popHashMap();

        return res;
    }

    @Override
    public Type type() throws TypeException {
        for (ClassNode classdec : classDeclarations) {
            classdec.type();
        }
        if (let != null)
            let.type();

        return in.type();
    }

    @Override
    public String codeGeneration() {
        String declaration = "";
        ArrayList<ClassNode> orderClassDeclarations = new ArrayList<ClassNode>();
        HashMap<String, ClassNode> classesAddedMap = new HashMap<String, ClassNode>();

        // this two loops are for order class declaration in top-down order to generate correct code and dispatch tables
        ListIterator iterator = classDeclarations.listIterator();
        while(iterator.hasNext()){
            ClassNode classDec = (ClassNode) iterator.next();
            if (classDec.getSuperClassID() == null || classDec.getSuperClassID().isEmpty()){
                orderClassDeclarations.add(classDec);
                classesAddedMap.put(classDec.getClassID(), classDec);
                iterator.remove();
            }
        }

        while (classDeclarations.size() != 0) {
            iterator = classDeclarations.listIterator();
            while(iterator.hasNext()){
                ClassNode childClassDec = (ClassNode) iterator.next();
                String fatherClassName = childClassDec.getSuperClassID();
                ClassNode fatherClassDec = classesAddedMap.get(fatherClassName);
                if (fatherClassDec != null){
                    orderClassDeclarations.add(childClassDec);
                    classesAddedMap.put(childClassDec.getClassID(), childClassDec);
                    iterator.remove();
                }
            }
        }

        for (ClassNode cl : orderClassDeclarations) {
            declaration += cl.codeGeneration();
        }

        if (let != null)
            return declaration + let.codeGeneration() + in.codeGeneration();
        else
            return declaration + in.codeGeneration();
    }

    @Override
    public ArrayList<INode> getChilds() {
        ArrayList<INode> childs = new ArrayList<>();
        childs.addAll(classDeclarations);

        if (let != null)
            childs.add(let);

        childs.add(in);
        return childs;
    }

    @Override
    public String toString() {
        return "class declarations";
    }

}
