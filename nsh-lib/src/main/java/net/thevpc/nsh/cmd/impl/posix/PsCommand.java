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
import net.thevpc.nsh.util.bundles.BytesSizeFormat;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NOut;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NArgName;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NMutableTableModel;
import net.thevpc.nuts.format.NTableModel;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        Options options = context.getOptions();
        NSession session = context.getSession();
        NArg a;
        if ((a = cmdLine.nextFlag(
                "-e",
                "-f",
                "-F",
                "-A",
                "-d",
                "-N",
                "-T"
        ).orNull()) != null) {
            for (char c : a.key().toCharArray()) {
                if (c == '-') {
                    //ignore
                } else {
                    options.flags.add(String.valueOf(c));
                }
            }
            return true;
        } else if (cmdLine.peek().get().isNonOption()) {
            String path = cmdLine.next(NArgName.of("options"))
                    .flatMap(NArg::asString).get();
            for (char c : path.toCharArray()) {
                options.flags.add(String.valueOf(c));
            }
            cmdLine.skip();
            return true;
        }
        return false;
    }

    @Override
    protected void main(NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        String userName = System.getProperty("user.name");
        List<NPsInfo> list = NPs.of().getResultList()
                .filter(x -> {
                    if (
                            options.flags.contains("A")
                                    || options.flags.contains("e")
                    ) {

                    } else if (options.flags.contains("a")) {
                        if (x.getStatusFlags().contains("session-leader")) {
                            return false;
                        }
                        String u = x.getTerminal();
                        if (NBlankable.isBlank(u)) {
                            return false;
                        }
                    } else {
                        String u = x.getUser();
                        if (!Objects.equals(u, userName)) {
                            return false;
                        }
                    }
                    if (!options.flags.contains("x")) {
                        String u = x.getTerminal();
                        if (NBlankable.isBlank(u)) {
                            return false;
                        }
                    }
                    if (options.flags.contains("r")) {
                        if (x.getStatus() != NpsStatus.RUNNING) {
                            return false;
                        }
                    }
                    return true;
                })
                .toList();
        String[] cols;
        if (
                options.flags.contains("u")
                        || options.flags.contains("f")
        ) {
            cols = new String[]{"user", "pid", "%cpu", "%mem", "vsz", "rss", "tty",
                    "stat", "start", "time", "command"};
        } else {
            cols = new String[]{"pid", "tty", "time", "cmd"};
        }
        switch (NSession.of().getOutputFormat().orDefault()) {
            case PLAIN: {
                NMutableTableModel model = NTableModel.of();
                model.addHeaderCells(Arrays.stream(cols).toArray());
                for (NPsInfo nPsInfo : list) {
                    model.addRow(
                            mapOf(nPsInfo, cols).values().toArray()
                    );
                }
                NOut.println(model);
                break;
            }
            default: {
                if (
                        options.flags.contains("u")
                                || options.flags.contains("f")
                ) {
                    NOut.println(list);

                }else {
                    NOut.println(list.stream().map(x->mapOf(x,cols)).collect(Collectors.toList()));
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
    }


    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        return nextOption(arg, cmdLine, context);
    }


}
