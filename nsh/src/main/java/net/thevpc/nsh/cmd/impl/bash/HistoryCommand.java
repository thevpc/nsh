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
import net.thevpc.nsh.history.NshHistory;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class HistoryCommand extends NshBuiltinDefault {

    public HistoryCommand() {
        super("history", NConstants.Support.DEFAULT_SUPPORT,Options.class);
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        NArg a;
        if (cmdLine.next("-c", "--clear").isPresent()) {
            options.action = Action.CLEAR;
            cmdLine.setCommandName(getName()).throwUnexpectedArgument();
            return true;
        } else if ((a = cmdLine.nextEntry("-d", "--delete").orNull()) != null) {
            options.action = Action.DELETE;
            options.ival = a.getValue().asIntValue().get();
            cmdLine.setCommandName(getName()).throwUnexpectedArgument();
            return true;
        } else if ((a = cmdLine.next("-D", "--remove-duplicates").orNull()) != null) {
            options.action = Action.REMOVE_DUPLICATES;
            cmdLine.setCommandName(getName()).throwUnexpectedArgument();
            return true;
        } else if ((a = cmdLine.next("-w", "--write").orNull()) != null) {
            options.action = Action.WRITE;
            if (a.isKeyValue()) {
                options.sval = a.getStringValue().get();
            } else if (!cmdLine.isEmpty()) {
                options.sval = cmdLine.next().flatMap(NLiteral::asStringValue).get();
            }
            cmdLine.setCommandName(getName()).throwUnexpectedArgument();
            return true;
        } else if ((a = cmdLine.next("-r", "--read").orNull()) != null) {
            options.action = Action.READ;
            if (a.isKeyValue()) {
                options.sval = a.getStringValue().get();
            } else if (!cmdLine.isEmpty()) {
                options.sval = cmdLine.next().flatMap(NLiteral::asStringValue).get();
            }
            cmdLine.setCommandName(getName()).throwUnexpectedArgument();
            return true;
        } else {
            if (cmdLine.peek().get().asIntValue().orElse(0) != 0) {
                options.action = Action.PRINT;
                options.ival = Math.abs(cmdLine.next().get().asIntValue().get());
                return true;
            }
        }
        return false;
    }

    @Override
    protected void main(NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        NshHistory shistory = context.getShell().getHistory();
        NSession session = context.getSession();
        switch (options.action) {
            case PRINT: {
                List<String> history = shistory.getElements(options.ival <= 0 ? 1000 : options.ival);
                int offset = shistory.size() - history.size();
                LinkedHashMap<String, String> result = new LinkedHashMap<>();
                for (int i = 0; i < history.size(); i++) {
                    String historyElement = history.get(i);
                    result.put(String.valueOf(offset + i + 1), historyElement);
                }
                NOut.println(result);
                break;
            }
            case CLEAR: {
                shistory.clear();
                return;
            }
            case REMOVE_DUPLICATES: {
                shistory.removeDuplicates();
                return;
            }
            case DELETE: {
                shistory.remove(options.ival - 1);
                return;
            }
            case WRITE: {
                try {
                    if (options.sval == null) {

                        shistory.save();
                    } else {
                        shistory.save(NPath.of(options.sval).toAbsolute(NWorkspace.of().getWorkspaceLocation()));
                    }
                } catch (IOException ex) {
                    throw new NExecutionException(NMsg.ofC("%s", ex), ex, NExecutionException.ERROR_2);
                }
                return;
            }
            case READ: {
                try {
                    if (options.sval == null) {
                        shistory.clear();
                        shistory.load();
                    } else {
                        shistory.load(NPath.of(options.sval).toAbsolute(NWorkspace.of().getWorkspaceLocation()));
                    }
                } catch (IOException ex) {
                    throw new NExecutionException(NMsg.ofC("%s", ex), ex, NExecutionException.ERROR_2);
                }
                return;
            }
            default: {
                throw new NUnsupportedArgumentException(NMsg.ofC("unsupported %s", String.valueOf(options.action)));
            }
        }
    }

    private enum Action {
        CLEAR,
        DELETE,
        REMOVE_DUPLICATES,
        WRITE,
        READ,
        PRINT
    }

    private static class Options {

        public String sval;
        int ival = -1;
        Action action = Action.PRINT;
    }
    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        return nextOption(arg, cmdLine, context);
    }
}
