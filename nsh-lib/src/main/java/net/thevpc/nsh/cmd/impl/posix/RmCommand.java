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

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nsh.cmd.NshBuiltinDefault;
import net.thevpc.nsh.eval.NshExecutionContext;
import net.thevpc.nsh.util.ShellHelper;
import net.thevpc.nuts.util.NAssert;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class RmCommand extends NshBuiltinDefault {

    public RmCommand() {
        super("rm", NConstants.Support.DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        NSession session = context.getSession();
        Options options = context.getOptions();
        NArg a;
        if ((a = cmdLine.nextFlag("-R").orNull()) != null) {
            options.R = a.getBooleanValue().get();
            return true;
        } else if (cmdLine.peek().get().isNonOption()) {
            options.files.add(ShellHelper.xfileOf(cmdLine.next().get().image(),
                    context.getDirectory(), session));
            return true;
        }
        return false;
    }

    @Override
    protected void main(NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        NAssert.requireNonBlank(options.files, "parameters");
//        ShellHelper.WsSshListener listener = options.verbose ? new ShellHelper.WsSshListener(context.getSession()) : null;
        for (NPath p : options.files) {
//            if (p instanceof SshXFile) {
//                ((SshXFile) p).setListener(listener);
//            }
            if (options.R) {
                p.deleteTree();
            } else {
                p.delete();
            }
        }
    }

    public static class Options {

        boolean R = false;
        boolean verbose = false;
        List<NPath> files = new ArrayList<>();
    }

    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        return nextOption(arg, cmdLine, context);
    }
}
