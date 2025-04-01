package net.thevpc.nsh.parser.nodes;

import net.thevpc.nsh.eval.NshContext;

public interface NshArgumentNode extends NshNode {
    String[] evalString(NshContext context);
}
