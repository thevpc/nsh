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
import net.thevpc.nsh.eval.NshExecutionContext;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NArgName;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.text.NMutableTableModel;
import net.thevpc.nuts.text.NTableModel;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class PsCommand extends NshBuiltinDefault {

    public PsCommand() {
        super("ps", NConstants.Support.DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        return cmdLine.matcher()
                .with("-e", "-A").matchTrueFlag((a) -> {
                    Options options = context.getOptions();
                    options.flags.add(a.key());
                    options.associatedWithTerminal = null;
                    options.running = null;
                    options.sessionLeader = null;
                    options.owned = null;
                })
                .with("-N").matchTrueFlag((v) -> {
                    Options options = context.getOptions();
                    options.flags.add(v.key());
                    options.negate = true;
                })
                .withNonOption().matchAny(v -> {
                    String path = cmdLine.next(NArgName.of("options"))
                            .flatMap(NArg::asString).get();
                    Options options = context.getOptions();
                    for (char c : path.toCharArray()) {
                        options.flags.add("+" + String.valueOf(c));
                        switch (c) {
                            case 'a':
                            case 'x': {
                                options.owned = null;
                                break;
                            }
                            case 'd': {
                                options.sessionLeader = false;
                                break;
                            }
                            case 'g': {
                                options.sessionLeader = null;
                                break;
                            }
                            case 'T':
                            case 't': {
                                options.associatedWithCurrentTerminal = true;
                                break;
                            }
                            case 'r': {
                                options.running = true;
                                break;
                            }
                            case 'f': {
                                //options.running = true;
                                break;
                            }
                        }
                    }
                    cmdLine.skip();
                }).anyMatch();
    }

    private boolean doAcceptNoNegate(NPsInfo x, Options options) {
        if (options.owned != null) {
            String u = x.getUser();
            String userName = System.getProperty("user.name");
            if (options.owned.booleanValue() != Objects.equals(u, userName)) {
                return false;
            }
        }
        if (options.associatedWithTerminal != null) {
            String u = x.getTerminal();
            if (options.associatedWithTerminal.booleanValue() != NBlankable.isNonBlank(u)) {
                return false;
            }
        }
        if (options.associatedWithCurrentTerminal != null) {
            // just ignore
        }
        if (options.sessionLeader != null) {
            if (options.sessionLeader.booleanValue() != x.getStatusFlags().contains("session-leader")) {
                return false;
            }
        }
        if (options.running != null) {
            if (options.running.booleanValue() != (x.getStatus() == NpsStatus.RUNNING)) {
                return false;
            }
        }
        return true;
    }

    private boolean doAccept(NPsInfo a, Options options) {
        boolean u = doAcceptNoNegate(a, options);
        return options.negate != u;
    }

    @Override
    protected void main(NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        List<NPsInfo> list = NPs.of().getResultList()
                .filter(x -> doAccept(x, options))
                .toList();
        String[] cols;
        if (
                options.flags.contains("+u")
                        || options.flags.contains("-f")
        ) {
            cols = new String[]{"user", "pid", "%cpu", "%mem", "vsz", "rss", "tty",
                    "stat", "start", "time", "command"};
        } else {
            cols = new String[]{"pid", "tty", "time", "cmd"};
        }
        switch (NSession.of().getOutputFormat().orDefault()) {
            case PLAIN: {
                NMutableTableModel model = NTableModel.of();
                model.addHeaderRow(Arrays.stream(cols).map(NText::of).toArray(NText[]::new));
                for (NPsInfo nPsInfo : list) {
                    model.addRow(
                            mapOf(nPsInfo, cols).values().stream().map(NText::of).toArray(NText[]::new)
                    );
                }
                NOut.println(model);
                break;
            }
            default: {
                if (
                        options.flags.contains("+u")
                                || options.flags.contains("+f")
                ) {
                    NOut.println(list);

                } else {
                    NOut.println(list.stream().map(x -> mapOf(x, cols)).collect(Collectors.toList()));
                }
                break;
            }
        }

    }

    private <K> Map<String, Object> mapOf(NPsInfo nPsInfo, String[] cols) {
        LinkedHashMap<String, Object> l = new LinkedHashMap<>();
        for (String col : cols) {
            switch (col.trim().toLowerCase()) {
                case "pid": {
                    l.put(col, nPsInfo.getPid());
                    break;
                }
                case "user": {
                    l.put(col, nPsInfo.getUser());
                    break;
                }
                case "%cpu": {
                    l.put(col, nPsInfo.getPercentCpu());
                    break;
                }
                case "%mem": {
                    l.put(col, nPsInfo.getPercentMem());
                    break;
                }
                case "vsz": {
                    l.put(col, nPsInfo.getVirtualMemorySize());
                    break;
                }
                case "rss": {
                    l.put(col, nPsInfo.getResidentSetSize());
                    break;
                }
                case "tty": {
                    l.put(col, NStringUtils.trim(nPsInfo.getTerminal()));
                    break;
                }
                case "stat": {
                    l.put(col, nPsInfo.getStatus());
                    break;
                }
                case "start": {
                    l.put(col, nPsInfo.getStartTime());
                    break;
                }
                case "time": {
                    l.put(col, nPsInfo.getTime());
                    break;
                }
                case "command": {
                    l.put(col,
                            nPsInfo.getCmdLineArgs() != null ?
                                    NCmdLine.of(nPsInfo.getCmdLineArgs())
                                    : nPsInfo.getCmdLine()
                    );
                    break;
                }
            }
        }
        return l;
    }


    private static class Options {
        Set<String> flags = new TreeSet<>();
        Boolean sessionLeader;
        Boolean associatedWithTerminal;
        Boolean associatedWithCurrentTerminal;
        Boolean owned = true;
        Boolean running;
        boolean negate;
    }


    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        return nextOption(arg, cmdLine, context);
    }


}
