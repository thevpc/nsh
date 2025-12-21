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
 *
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

import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nsh.cmd.NshBuiltin;
import net.thevpc.nsh.cmd.NshBuiltinDefault;
import net.thevpc.nsh.eval.NshExecutionContext;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;

import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class EnableCommand extends NshBuiltinDefault {

    public EnableCommand() {
        super("enable", Options.class);
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        final NArg a = cmdLine.peek().get();
        if (a.isOption()) {
            if (a.key().equals("--sort")) {
                options.displayOptions.add(a.toString());
                return true;
            }
        } else if (a.isOption()) {
            switch(a.key()) {
                case "-a": {
                    return cmdLine.matcher().matchFlag((v) -> options.a = v.booleanValue()).anyMatch();
                }
                case "-d": {
                    return cmdLine.matcher().matchFlag((v) -> options.d = v.booleanValue()).anyMatch();
                }
                case "-n": {
                    return cmdLine.matcher().matchFlag((v) -> options.n = v.booleanValue()).anyMatch();
                }
                case "-p": {
                    return cmdLine.matcher().matchFlag((v) -> options.p = v.booleanValue()).anyMatch();
                }
                case "-s": {
                    return cmdLine.matcher().matchFlag((v) -> options.s = v.booleanValue()).anyMatch();
                }
                case "-f": {
                    return cmdLine.matcher().matchEntry((v) -> options.file = v.stringValue()).anyMatch();
                }
            }
        } else {
            options.names.add(cmdLine.next().get().image());
            return true;
        }
        return false;
    }

    @Override
    protected void main(NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        if (options.p || options.names.isEmpty()) {
            Map<String, String> result = new LinkedHashMap<>();
            for (NshBuiltin command : context.builtins().getAll()) {
                result.put(command.getName(), command.isEnabled() ? "enabled" : "disabled");
            }
            switch (context.getSession().getOutputFormat().orDefault()) {
                case PLAIN: {
                    for (Map.Entry<String, String> entry : result.entrySet()) {
                        NOut.println(entry.getValue() + " " + entry.getKey());
                    }
                    //if list
//                    for (String s : ((List<String>) context.getResult())) {
//                        context.out().print(NMsg.ofC("%s%n",
//                                text.builder().append("enable: ", NutsTextStyle.error())
//                                        .append(s, NutsTextStyle.primary5())
//                                        .append(" ")
//                                        .append("not a shell builtin", NutsTextStyle.error())
//                        );
//                    }
                    break;
                }
                default: {
                    NOut.println(result);
                }
            }
        } else if (options.n) {
            List<String> nobuiltin = new ArrayList<>();
            for (String name : options.names) {
                NshBuiltin c = context.builtins().find(name);
                if (c == null) {
                    nobuiltin.add(name);
                } else {
                    c.setEnabled(false);
                }
            }
            if (!nobuiltin.isEmpty()) {
                throwExecutionException(nobuiltin, 1, context.getSession());
            }
        }
    }

    private static class Options {

        String file;
        boolean a;
        boolean d;
        boolean n;
        boolean p;
        boolean s;
        Set<String> names = new LinkedHashSet<String>();
        List<String> displayOptions = new ArrayList<String>();
    }


    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        return nextOption(arg, cmdLine, context);
    }
}
