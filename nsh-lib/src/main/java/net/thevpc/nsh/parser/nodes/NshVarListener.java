package net.thevpc.nsh.parser.nodes;

import net.thevpc.nsh.eval.NshContext;

public interface NshVarListener {
    default void varAdded(NshVar nshVar, NshVariables vars, NshContext context) {

    }

    default void varValueUpdated(NshVar nshVar, String oldValue, NshVariables vars, NshContext context) {

    }

    default void varExportUpdated(NshVar nshVar, boolean oldValue, NshVariables vars, NshContext context) {

    }

    default void varRemoved(NshVar nshVar, NshVariables vars, NshContext context) {

    }
}
