/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nsh.sys;

import net.thevpc.nsh.eval.NshContext;

/**
 *
 * @author thevpc
 */
public interface NshExternalExecutor {

    int execExternalCommand(String[] command, NshContext context);
}
