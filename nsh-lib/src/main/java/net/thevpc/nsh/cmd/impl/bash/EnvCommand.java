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
package net.thevpc.nsh.cmd.impl.bash;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nsh.cmd.NshBuiltinDefault;
import net.thevpc.nsh.eval.NshExecutionContext;
import net.thevpc.nuts.util.NBlankable;

import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class EnvCommand extends NshBuiltinDefault {

    public EnvCommand() {
        super("env", NConstants.Support.DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        NArg a = cmdLine.peek().get();
        switch (options.readStatus) {
            case 0: {
                switch (a.key()) {
                    case "--sort": {
                        return cmdLine.matcher().matchFlag((v) -> options.sort = v.booleanValue()).anyMatch();
                    }
                    case "--external":
                    case "--spawn":
                    case "-x": {
                        return cmdLine.matcher().matchTrueFlag((v) -> options.executionType = NExecutionType.SPAWN).anyMatch();
                    }
                    case "--embedded":
                    case "-b": {
                        return cmdLine.matcher().matchTrueFlag((v) -> options.executionType = NExecutionType.EMBEDDED).anyMatch();
                    }
                    case "--system": {
                        return cmdLine.matcher().matchTrueFlag((v) -> options.executionType = NExecutionType.SYSTEM).anyMatch();
                    }
                    case "--current-user": {
                        return cmdLine.matcher().matchTrueFlag((v) -> options.runAs = NRunAs.currentUser()).anyMatch();
                    }
                    case "--as-root": {
                        return cmdLine.matcher().matchTrueFlag((v) -> options.runAs = NRunAs.root()).anyMatch();
                    }
                    case "--sudo": {
                        return cmdLine.matcher().matchTrueFlag((v) -> options.runAs = NRunAs.sudo()).anyMatch();
                    }
                    case "--as-user": {
                        return cmdLine.matcher().matchEntry((v) -> options.runAs = NRunAs.user(v.stringValue())).anyMatch();
                    }
                    case "-C":
                    case "--chdir": {
                        return cmdLine.matcher().matchEntry((v) -> options.dir = v.stringValue()).anyMatch();
                    }
                    case "-u":
                    case "--unset": {
                        return cmdLine.matcher().matchEntry((v) -> options.unsetVers.add(v.stringValue())).anyMatch();
                    }
                    case "-i":
                    case "--ignore-environment": {
                        cmdLine.matcher().matchFlag((v) -> options.ignoreEnvironment = v.booleanValue()).anyMatch();
                    }
                    case "-": {
                        cmdLine.skip();
                        options.readStatus = 1;
                        return true;
                    }
                    default: {
                        if (a.isKeyValue()) {
                            options.newEnv.put(a.key(), a.getStringValue().get());
                            cmdLine.skip();
                            options.readStatus = 1;
                            return true;
                        } else if (a.isOption()) {
                            return false;
                        } else {
                            options.command.add(a.asString().get());
                            cmdLine.skip();
                            options.readStatus = 2;
                            return true;
                        }
                    }
                }
            }
            case 1: {
                if (a.isKeyValue()) {
                    options.newEnv.put(a.key(), a.getStringValue().get());
                } else {
                    options.command.add(a.asString().get());
                    options.readStatus = 2;
                }
                cmdLine.skip();
                return true;
            }
            case 2: {
                options.command.add(a.asString().get());
                cmdLine.skip();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void main(NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        if (options.sort) {
            context.getSession().addOutputFormatOptions("--sort");
        }
        SortedMap<String, String> env = new TreeMap<>();
        if (!options.ignoreEnvironment) {
            env.putAll((Map) context.vars().getAll());
        }
        for (String v : options.unsetVers) {
            env.remove(v);
        }
        env.putAll(options.newEnv);
        if (options.command.isEmpty()) {
            if (context.getSession().isPlainOut()) {
                for (Map.Entry<String, String> e : env.entrySet()) {
                    NOut.println(e.getKey() + "=" + e.getValue());
                }
            } else {
                NOut.println(env);
            }
        } else {
            final NExecCmd e = NExecCmd.of().addCommand(options.command)
                    .setEnv(env)
                    .failFast();
            if (!NBlankable.isBlank(options.dir)) {
                e.setDirectory(NPath.of(options.dir));
            }
            if (options.executionType != null) {
                e.setExecutionType(options.executionType);
            }
            e.run();
        }
    }

    public static class Options {

        int readStatus = 0;
        LinkedHashMap<String, String> newEnv = new LinkedHashMap<>();
        List<String> command = new ArrayList<String>();
        Set<String> unsetVers = new HashSet<String>();
        boolean sort = true;
        boolean ignoreEnvironment = false;
        String dir = null;
        NExecutionType executionType = null;
        NRunAs runAs = null;
    }

    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        return nextOption(arg, cmdLine, context);
    }
}
