/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nsh.parser.nodes;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author thevpc
 */
public class DefaultNshFunctionManager implements NshFunctionManager {

    public final Map internalFunctions = new HashMap();

    @Override
    public NshFunction findFunction(String n) {
        return (NshFunction) internalFunctions.get(n);
    }

    @Override
    public void declareFunction(NshFunction cmd) {
        synchronized (internalFunctions) {
            internalFunctions.put(cmd.getName(), cmd);
        }
    }

    @Override
    public void declareFunctions(NshFunction... cmds) {
        synchronized (internalFunctions) {
            for (NshFunction cmd : cmds) {
                internalFunctions.put(cmd.getName(), cmd);
            }
        }
    }

    @Override
    public boolean containsFunction(String cmd) {
        synchronized (internalFunctions) {
            return internalFunctions.containsKey(cmd);
        }
    }

    @Override
    public boolean unset(String cmd) {
        synchronized (internalFunctions) {
            return internalFunctions.remove(cmd) != null;
        }
    }

    @Override
    public NshFunction[] getAll() {
        return (NshFunction[]) internalFunctions.values().toArray(new NshFunction[0]);
    }
}
