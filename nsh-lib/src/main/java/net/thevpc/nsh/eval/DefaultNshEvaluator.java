/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nsh.eval;

import net.thevpc.nsh.err.DefaultErrorHandler;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.io.NTerminal;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nsh.err.NshException;
import net.thevpc.nsh.err.NshUniformException;
import net.thevpc.nsh.parser.nodes.NshCommandNode;
import net.thevpc.nsh.util.NshNonBlockingInputStream;
import net.thevpc.nsh.util.NshNonBlockingInputStreamAdapter;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.text.NMsg;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.logging.Level;

/**
 * @author thevpc
 */
public class DefaultNshEvaluator extends NshEvaluatorBase {

    @Override
    public int evalBinaryPipeOperation(NshCommandNode left, NshCommandNode right, NshContext context) {
        final NPrintStream nout;
        final PipedOutputStream out;
        final PipedInputStream in;
        final NshNonBlockingInputStream in2;
        try {
            out = new PipedOutputStream();
            nout = NPrintStream.of(out, NTerminalMode.FORMATTED,null);
            in = new PipedInputStream(out, 1024);
            in2 = (in instanceof NshNonBlockingInputStream) ? (NshNonBlockingInputStream) in : new NshNonBlockingInputStreamAdapter("jpipe-" + right.toString(), in);
        } catch (IOException ex) {
            throw new NshException(ex, 1);
        }
        final NshContext leftContext = context.nsh().createNewContext(context).setOut(nout.asPrintStream());
        final NshUniformException[] a = new NshUniformException[2];
        Thread j1 = new Thread() {
            @Override
            public void run() {
                try {
                    context.nsh().evalNode(left, leftContext);
                } catch (NshUniformException e) {
                    if (e.isQuit()) {
                        e.throwQuit();
                        return;
                    }
                    a[0] = e;
                }
                in2.noMoreBytes();
            }

        };
        j1.start();
        NshContext rightContext = context.nsh().createNewContext(context).setIn((InputStream) in2);
        try {
            context.nsh().evalNode(right, rightContext);
        } catch (NshUniformException e) {
            if (e.isQuit()) {
                e.throwQuit();
                return 0;
            }
            a[1] = e;
        }
        try {
            j1.join();
        } catch (Exception ex) {
            NLog.of(DefaultNshEvaluator.class).log(NMsg.ofC("failed : %s", ex).asFinestFail(ex));
        }
        if (a[1] != null) {
            a[1].throwAny();
        }
        return 0;
    }

    @Override
    public String evalCommandAndReturnString(NshCommandNode command, NshContext context) {
        DefaultNshContext newCtx = (DefaultNshContext) context.nsh().createNewContext(context);
        NSession session = newCtx.getSession().copy();
        newCtx.setSession(session);
        session.setLogTermLevel(Level.OFF);

        NTerminal out = NTerminal.ofMem();
        session.setTerminal(out);
        context.nsh().evalNode(command, newCtx);
        String str = evalFieldSubstitutionAfterCommandSubstitution(out.out().toString(), context);
        String s = context.nsh().escapeString(str);
        context.err().print(session.err().toString());
        return s;
    }
}
