package node;

import exception.TypeException;
import exception.UndeclaredMethodException;
import exception.UndeclaredVarException;
import grammar.FOOLParser;
import main.SemanticError;
import symbol_table.Environment;
import symbol_table.SymbolTableEntry;
import type.ClassType;
import type.FunType;
import type.InstanceType;
import type.Type;

import java.util.ArrayList;

public class MethodCallNode extends FunCallNode {

    private int objectOffset;
    private int objectNestingLevel;
    private int methodOffset;
    private int nestinglevel;

    private String objectID;
    private String methodID;
    private ArgumentsNode args;
    private Type methodType;

    public MethodCallNode(FOOLParser.MethodExpContext ctx, String objectID, String methodID, ArgumentsNode args) {
        super(ctx.funcall(), methodID, args.getChilds());
        this.objectID = objectID;
        this.methodID = methodID;
        this.args = args;
    }

    @Override
    public ArrayList<SemanticError> checkSemantics(Environment env) {
        ArrayList<SemanticError> res = new ArrayList<>();
        this.nestinglevel = env.getNestingLevel();
        try {

            ClassType classType = null;
            // Calcolo gli offset per recuperare l'oggetto
            if (objectID.equals("this")) {
                Type objectType = env.getLatestClassEntry().getType();
                // TODO: cosi' funziona sempre credo, pero' sarebbe meglio farlo meno hardcoded
                // Se il metodo e' chiamato su this, l'offset rispetto a $fp e' sempre 0
                this.objectOffset = 0;
                // L'oggetto e' sempre al livello dei parametri di metodo, ovvero 3
                this.objectNestingLevel = 3;
                if (objectType instanceof ClassType) {
                    classType = (ClassType) objectType;
                } else {
                    res.add(new SemanticError("Can't call this outside a class"));
                }
            } else {
                SymbolTableEntry objectSEntry = env.getLatestEntryOf(objectID);
                Type objectType = objectSEntry.getType();
                this.objectOffset = objectSEntry.getOffset();
                this.objectNestingLevel = objectSEntry.getNestinglevel();
                // Controllo che il metodo sia stato chiamato su un oggetto
                if (objectType instanceof InstanceType) {
                    classType = ((InstanceType) objectType).getClassType();
                } else {
                    res.add(new SemanticError("Method " + methodID + " called on a non-object type"));
                }
            }

            // Se il metodo viene chiamato su this, vuol dire che stiamo facendo la semantic analysis
            // della classe, quindi prendo l'ultima entry aggiunta alla symbol table
            SymbolTableEntry classEntry = objectID.equals("this")
                    ? env.getLatestClassEntry()
                    : env.getLatestEntryOf(classType.getClassID());

            ClassType objectClass = (ClassType) classEntry.getType();
            this.methodOffset = objectClass.getOffsetOfMethod(methodID);
            this.methodType = objectClass.getTypeOfMethod(methodID);
            // Controllo che il metodo esista all'interno della classe
            if (this.methodType == null) {
                res.add(new SemanticError("Object " + objectID + " doesn't have a " + methodID + " method."));
            }

            FunType t = (FunType) this.methodType;
            ArrayList<Type> p = t.getParams();
            if (!(p.size() == args.size())) {
                res.add(new SemanticError("Wrong number of parameters in the invocation of " + id));
            }

            res.addAll(args.checkSemantics(env));

        } catch (UndeclaredVarException | UndeclaredMethodException e) {
            res.add(new SemanticError(e.getMessage()));
        }

        return res;
    }

    @Override
    public Type type() throws TypeException {
        FunType t = (FunType) this.methodType;
        ArrayList<Type> p = t.getParams();

        for (int i = 0; i < args.size(); i++)
            if (!args.get(i).type().isSubTypeOf(p.get(i)))
                throw new TypeException("Wrong type for " + (i + 1) + "-th parameter in the invocation of " + id, ctx);

        return t.getReturnType();
    }

    @Override
    public String codeGeneration() {
        StringBuilder parCode = new StringBuilder();
        for (int i = args.size() - 1; i >= 0; i--)
            parCode.append(args.get(i).codeGeneration());

        StringBuilder getAR = new StringBuilder();

        for (int i = 0; i < nestinglevel - objectNestingLevel; i++)
            getAR.append("lw\n");

        return "lfp\n"                                  // carico il frame pointer (perche'?)
                + parCode                               // carico i parametri
                + "push " + objectOffset + "\n"         // carico l'offset dell'oggetto nello scope di definizione
                + "lfp\n"                               // carico il frame pointer
                + getAR                                 // faccio gli lw necessari fino a trovarmi sullo stack l'indirizzo in memoria del frame dove e' definito l'oggetto
                + "add\n"                               // faccio $fp + offset per ottenere l'indirizzo in memoria dell'oggetto
                + "lw\n"                                // carico il valore dell'oggetto sullo stack
                + "copy\n"                              // copio il valore sopra (l'indirizzo di memoria nel quale si trova l'indirizzo della dispatch table)
                + "lw\n"                                // carico l'indirizzo della dispatch table sullo stack
                + "push " + (methodOffset - 1) + "\n"   // carico l'offset del metodo rispetto all'inizio della dispatch table
                + "add" + "\n"                          // carico sullo stack dispatch_table_start + offset
                + "lc\n"                                // trovo l'indirizzo del metodo
                + "js\n";                               // salto all'istruzione dove e' definito il metodo e salvo $ra
    }

    @Override
    public String toString() {
        return objectID + "." + methodID + "()";
    }

}
