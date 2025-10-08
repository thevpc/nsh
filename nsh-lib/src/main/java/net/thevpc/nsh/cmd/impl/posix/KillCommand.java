/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
package net.thevpc.nsh.cmd.impl.posix;

import net.thevpc.nsh.cmd.NshBuiltinDefault;
import net.thevpc.nsh.err.NshException;
import net.thevpc.nsh.eval.NshExecutionContext;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NArgName;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPs;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.text.NMsg;

import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class KillCommand extends NshBuiltinDefault {

    public KillCommand() {
        super("kill", DEFAULT_SCORE, Options.class);
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        NArg a;
        if ((a = cmdLine.nextFlag("-9").orNull()) != null) {
            options.signal = -9;
            return true;
        } else if (cmdLine.peek().get().isNonOption()) {
            String pid = cmdLine.next(NArgName.of("file"))
                    .flatMap(NArg::asString).get();
            options.pids.add(pid);
            cmdLine.skip();
            return true;
        }
        return false;
    }

    @Override
    protected void main(NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        if (options.pids.isEmpty()) {
            throw new NshException(NMsg.ofC("kill: usage: kill [-s sigspec | -n signum | -sigspec] pid | jobspec ... or kill -l [sigspec]"), 2);
        }
        for (String pid : options.pids) {
            NPs.of().killProcess(pid);
        }
    }


    private static class Options {

        int signal = -9;
        List<String> pids = new ArrayList<>();
    }


    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        return nextOption(arg, cmdLine, context);
    }
}
