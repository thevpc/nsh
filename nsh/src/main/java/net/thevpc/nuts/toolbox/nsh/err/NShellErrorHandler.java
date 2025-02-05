/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.err;

import net.thevpc.nuts.toolbox.nsh.eval.NShellContext;

/**
 *
 * @author thevpc
 */
public interface NShellErrorHandler {

    int errorToCode(Throwable th);
    
    String errorToMessage(Throwable th);

    void onError(String message, Throwable th, NShellContext context);

    boolean isQuitException(Throwable th);
}
