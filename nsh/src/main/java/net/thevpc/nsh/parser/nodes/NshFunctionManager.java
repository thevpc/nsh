/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nsh.parser.nodes;

/**
 *
 * @author thevpc
 */
public interface NshFunctionManager {

    NshFunction findFunction(String command);

    boolean containsFunction(String cmd);

    void declareFunction(NshFunction cmd);

    void declareFunctions(NshFunction... cmds);

    boolean unset(String name);

    NshFunction[] getAll();

}
