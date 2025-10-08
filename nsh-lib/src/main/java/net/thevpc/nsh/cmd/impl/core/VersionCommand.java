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
package net.thevpc.nsh.cmd.impl.core;

import net.thevpc.nuts.app.NApp;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.text.NVersionFormat;
import net.thevpc.nsh.cmd.NshBuiltinCore;
import net.thevpc.nsh.eval.NshExecutionContext;

/**
 * Created by vpc on 1/7/17.
 */
public class VersionCommand extends NshBuiltinCore {

    public VersionCommand() {
        super("version", DEFAULT_SCORE, Options.class);
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        if (options.version == null) {
            options.version = NVersionFormat.of();
        }
        return options.version.configureFirst(cmdLine);
    }

    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        if (options.version == null) {
            options.version = NVersionFormat.of();
        }
        return options.version.configureFirst(cmdLine);
    }

    @Override
    protected void main(NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        if (options.version == null) {
            options.version = NVersionFormat.of();
        }
        if(context.getSession().isPlainOut()){
            context.out().println( NApp.of().getId().get().getVersion().getValue());
        }else {
            options.version
                    .addProperty("app-version", NApp.of().getId().get().getVersion().getValue())
                    .println(context.out());
        }
    }

    private static class Options {
        NVersionFormat version;
    }
}
