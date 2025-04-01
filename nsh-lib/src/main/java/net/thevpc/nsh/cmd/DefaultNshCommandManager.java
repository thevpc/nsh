/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nsh.cmd;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 *
 * @author thevpc
 */
public class DefaultNshCommandManager implements NshBuiltinManager {

    public final Map internalCommands = new HashMap();

    @Override
    public NshBuiltin find(String n) {
        return (NshBuiltin) internalCommands.get(n);
    }

    @Override
    public void set(NshBuiltin cmd) {
        synchronized (internalCommands) {
            internalCommands.put(cmd.getName(), cmd);
        }
    }

    @Override
    public void set(NshBuiltin... cmds) {
        synchronized (internalCommands) {
            for (NshBuiltin cmd : cmds) {
                internalCommands.put(cmd.getName(), cmd);
            }
        }
    }

    @Override
    public boolean contains(String cmd) {
        synchronized (internalCommands) {
            return internalCommands.containsKey(cmd);
        }
    }

    @Override
    public boolean unset(String cmd) {
        synchronized (internalCommands) {
            return internalCommands.remove(cmd) != null;
        }
    }

    @Override
    public NshBuiltin[] getAll() {
        return (NshBuiltin[]) internalCommands.values().toArray(new NshBuiltin[0]);
    }

    public NshBuiltin get(String cmd) {
        NshBuiltin command = find(cmd);
        if (command == null) {
            throw new NoSuchElementException("NshCommandNode not found : " + cmd);
        }
        if (!command.isEnabled()) {
            throw new NoSuchElementException("NshCommandNode disabled : " + cmd);
        }
        return command;
    }
}
