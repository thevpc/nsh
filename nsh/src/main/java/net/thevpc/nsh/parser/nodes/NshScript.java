package net.thevpc.nsh.parser.nodes;

import net.thevpc.nsh.eval.NshContext;

public class NshScript implements NshCommandNode {
    private final NshCommandNode root;

    public NshScript(NshCommandNode root) {
        this.root = root;
    }

    @Override
    public int eval(NshContext context) {
        return root.eval(context);
    }

    public NshCommandNode getRoot() {
        return root;
    }
}
