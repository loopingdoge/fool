// Generated from C:/Users/Alberto/workspace/fool/src/parser\SVM.g4 by ANTLR 4.7
package grammar;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link SVMParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 *            operations with no return type.
 */
public interface SVMVisitor<T> extends ParseTreeVisitor<T> {
    /**
     * Visit a parse tree produced by {@link SVMParser#assembly}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAssembly(SVMParser.AssemblyContext ctx);
}