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
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.io.NAsk;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NIn;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nsh.cmd.NshBuiltinDefault;
import net.thevpc.nsh.eval.NshExecutionContext;
import net.thevpc.nsh.util.ShellHelper;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class RmCommand extends NshBuiltinDefault {

    public RmCommand() {
        super("rm", Options.class);
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        NArg a;
        // recursive flags
        if ((a = cmdLine.nextFlag("-r", "-R", "--recursive").orNull()) != null) {
            options.recursive = a.getBooleanValue().get();
            return true;
        }
        // force
        if ((a = cmdLine.nextFlag("-f", "--force").orNull()) != null) {
            options.force = a.getBooleanValue().get();
            return true;
        }
        // interactive
        if ((a = cmdLine.nextFlag("-i", "--interactive").orNull()) != null) {
            options.interactive = a.getBooleanValue().get();
            return true;
        }
        // verbose
        if ((a = cmdLine.nextFlag("-v", "--verbose").orNull()) != null) {
            options.verbose = a.getBooleanValue().get();
            return true;
        }
        return false;
    }

    @Override
    protected void main(NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        NAssert.requireNamedNonBlank(options.files, "parameters");

        for (NPath p : options.files) {
            // Check existence (unless force)
            if (!options.force && !p.exists()) {
                context.err().println(NMsg.ofC("rm: cannot remove '%s': No such file or directory", p));
                continue;
            }
            // Interactive prompt
            if (options.interactive && p.exists()) {
                if(!NIn.ask().forBoolean(NMsg.ofC("rm: remove %s? ", p)).defaultValue(false).booleanValue()){
                    continue;
                }
            }
            try {
                if (options.recursive) {
                    if (options.verbose) {
                        context.out().println(NMsg.ofC("removed directory: %s", p));
                    }
                    p.deleteTree();
                } else {
                    if (p.isDirectory() && !options.force) {
                        context.err().println(NMsg.ofC("rm: cannot remove '%s': Is a directory", p));
                        continue;
                    }
                    if (options.verbose) {
                        context.out().println(NMsg.ofC("removed: %s", p));
                    }
                    p.delete();
                }
            } catch (NIOException e) {
                if (!options.force) {
                    context.err().println(NMsg.ofC("rm: cannot remove '%s': %s", p, e.getMessage()));
                }
            }
        }
    }

    public static class Options {

        boolean recursive = false;
        boolean force = false;
        boolean interactive = false;
        boolean verbose = false;
        List<NPath> files = new ArrayList<>();
    }

    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        options.files.add(ShellHelper.xfileOf(cmdLine.next().get().image(),
                context.getDirectory()));
        return true;
    }
}
