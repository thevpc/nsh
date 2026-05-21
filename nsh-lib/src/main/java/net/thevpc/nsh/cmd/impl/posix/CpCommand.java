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

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;


import net.thevpc.nuts.command.NExecutionException;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nsh.cmd.NshBuiltinDefault;
import net.thevpc.nsh.eval.NshExecutionContext;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;

import java.util.ArrayList;
import java.util.List;

@NComponentScope(NScopeType.WORKSPACE)
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class CpCommand extends NshBuiltinDefault {

    public CpCommand() {
        super("cp", Options.class);
    }


    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        options.files.add(cmdLine.next().get().toString());
        return true;
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        switch (cmdLine.peek().get().key()) {
            case "--mkdir": {
                return cmdLine.matcher().withAny().matchFlag((v) -> options.mkdir = v.booleanValue()).anyMatch();
            }
            case "-r":
            case "-R":
            case "--recursive": {
                return cmdLine.matcher().withAny().matchFlag((v) -> options.recursive = v.booleanValue()).anyMatch();
            }
        }
        return false;
    }

    @Override
    protected void main(NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        for (String value : options.files) {
            NAssert.requireNamedNonBlank(value, "file path");
            options.xfiles.add(NPath.of((value.contains("://") ? value :
                    NPath.of(value).toAbsolute(NWorkspace.of().workspaceLocation()).toString()
            )));
        }
        if (options.xfiles.size() < 2) {
            throw new NExecutionException(NMsg.ofPlain("missing parameters"), NExecutionException.ERROR_2);
        }

//        options.sshlistener = new ShellHelper.WsSshListener(session);
        for (int i = 0; i < options.xfiles.size() - 1; i++) {
            copy(options.xfiles.get(i), options.xfiles.get(options.xfiles.size() - 1), options, context);
        }
    }

    public void copy(NPath from, NPath to, Options o, NshExecutionContext context) {
        NCp ccp = NCp.of()
                .from(from)
                .to(to)
                .setRecursive(o.recursive)
                .mkdirs(o.mkdir);
        ccp.run();
    }

    public static class Options {

        boolean mkdir;
        boolean recursive;
//        ShellHelper.WsSshListener sshlistener;
        List<String> files = new ArrayList<>();
        List<NPath> xfiles = new ArrayList<>();
    }


}
