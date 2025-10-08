package net.thevpc.nsh.sys;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nsh.eval.NshContext;
import net.thevpc.nsh.err.NshException;

public class NshNoExternalExecutor implements NshExternalExecutor {
    @Override
    public int execExternalCommand(String[] command, NshContext context) {
        throw new NshException(NMsg.ofC("not found %s", command[0]), 101);
    }
}
