/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nsh.cmd;

/**
 *
 * @author thevpc
 */
public interface NshBuiltinManager {

    NshBuiltin find(String command);
    
    NshBuiltin get(String command);

    boolean contains(String cmd);

    void set(NshBuiltin cmd);

    void set(NshBuiltin... cmds);

    boolean unset(String name);

    NshBuiltin[] getAll();

}
