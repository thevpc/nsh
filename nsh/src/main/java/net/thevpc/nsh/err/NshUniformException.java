/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nsh.err;

import net.thevpc.nuts.util.NMsg;

/**
 *
 * @author thevpc
 */
public class NshUniformException extends NshException {

    private boolean quit;

    public NshUniformException(int code, boolean quit, Throwable cause) {
        super(NMsg.ofPlain("error"),cause,code);
        this.quit = quit;
    }

    public void throwQuit() {
        if (getCause() instanceof NshQuitException) {
            throw (NshQuitException) getCause();
        }
        if (getCause() instanceof RuntimeException) {
            throw (RuntimeException) getCause();
        }
        throw new NshQuitException(getCause(), getExitCode());
    }

    public void throwAny() {
        if (getCause() instanceof NshQuitException) {
            throw (NshQuitException) getCause();
        }
        if (getCause() instanceof RuntimeException) {
            throw (RuntimeException) getCause();
        }
        throw new NshException(getFormattedMessage(), getCause(),getExitCode());
    }

    public boolean isQuit() {
        return quit;
    }

}
