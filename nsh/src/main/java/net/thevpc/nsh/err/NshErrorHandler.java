/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nsh.err;

import net.thevpc.nsh.eval.NshContext;

/**
 *
 * @author thevpc
 */
public interface NshErrorHandler {

    int errorToCode(Throwable th);
    
    String errorToMessage(Throwable th);

    void onError(String message, Throwable th, NshContext context);

    boolean isQuitException(Throwable th);
}
