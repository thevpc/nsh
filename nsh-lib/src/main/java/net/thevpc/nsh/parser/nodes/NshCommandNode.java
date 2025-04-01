package net.thevpc.nsh.parser.nodes;

import net.thevpc.nsh.eval.NshContext;

public interface NshCommandNode extends NshNode {
    int eval(NshContext context);
}
