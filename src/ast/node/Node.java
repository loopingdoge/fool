package ast.node;

import org.antlr.v4.runtime.ParserRuleContext;

public abstract class Node implements INode {

    protected ParserRuleContext ctx;

    public Node(ParserRuleContext ctx) {
        this.ctx = ctx;
    }

}
