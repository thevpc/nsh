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
package net.thevpc.nsh.cmd.impl.bash;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nsh.cmd.NshBuiltinDefault;
import net.thevpc.nsh.eval.NshExecutionContext;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class UnaliasCommand extends NshBuiltinDefault {

    public UnaliasCommand() {
        super("unalias", Options.class);
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        NArg aa = cmdLine.peek().get();
        if (aa.isOption()) {
            if (aa.key().equals("-a")) {
                return cmdLine.matcher().matchFlag((v) -> options.all= v.booleanValue()).anyMatch();
            }
        } else {
            options.list.addAll(Arrays.asList(cmdLine.toStringArray()));
            cmdLine.skipAll();
            return true;
        }
        return false;
    }

    @Override
    protected void main(NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        if (options.all) {
            for (String k : context.aliases().getAll()) {
                context.aliases().set(k, null);
            }
        } else {
            for (String k : options.list) {
                context.aliases().set(k, null);
            }
        }
    }

    private static class Options {

        boolean all;
        Set<String> list = new HashSet<>();
    }
    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        return nextOption(arg, cmdLine, context);
    }
}
