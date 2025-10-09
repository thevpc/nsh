/**
 * ====================================================================
 * Doovos (Distributed Object Oriented Operating System)
 * <p>
 * Doovos is a new Open Source Distributed Object Oriented Operating System
 * Design and implementation based on the Java Platform. Actually, it is a try
 * for designing a distributed operation system in top of existing
 * centralized/network OS. Designed OS will follow the object oriented
 * architecture for redefining all OS resources (memory,process,file
 * system,device,...etc.) in a highly distributed context. Doovos is also a
 * distributed Java virtual machine that implements JVM specification on top the
 * distributed resources context.
 * <p>
 * Doovos BIN is a standard implementation for Doovos boot sequence, shell and
 * common application tools. These applications are running onDoovos guest JVM
 * (distributed jvm).
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
 * <br> ====================================================================
 */
package net.thevpc.nsh;

import net.thevpc.nsh.err.*;
import net.thevpc.nsh.eval.*;
import net.thevpc.nsh.history.NoHistory;
import net.thevpc.nsh.options.NshOptions;
import net.thevpc.nsh.parser.nodes.*;
import net.thevpc.nuts.app.NApp;
import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineHistory;
import net.thevpc.nuts.command.NCommandConfig;
import net.thevpc.nuts.command.NCustomCmd;
import net.thevpc.nuts.command.NExecutionException;
import net.thevpc.nuts.command.NFetchCmd;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.elem.NElementDescribables;

import net.thevpc.nuts.platform.NLauncherOptions;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.io.*;

import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.security.NWorkspaceSecurityManager;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nsh.options.autocomplete.NshAutoCompleter;
import net.thevpc.nsh.cmd.resolver.NCommandTypeResolver;
import net.thevpc.nsh.cmd.resolver.NshCommandTypeResolver;
import net.thevpc.nsh.cmd.NshBuiltin;
import net.thevpc.nsh.cmd.NshBuiltinCore;
import net.thevpc.nsh.cmd.NshBuiltinDefault;
import net.thevpc.nsh.history.DefaultNshHistory;
import net.thevpc.nsh.history.NshHistory;
import net.thevpc.nsh.options.DefaultNshOptionsParser;
import net.thevpc.nsh.options.NshOptionsParser;
import net.thevpc.nsh.parser.NshParser;
import net.thevpc.nsh.sys.NshToNutsExternalExecutor;
import net.thevpc.nsh.sys.NshExternalExecutor;
import net.thevpc.nsh.util.ByteArrayPrintStream;
import net.thevpc.nsh.util.MemResult;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.time.NClock;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Year;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class Nsh {
    private static final HashSet<String> CONTEXTUAL_BUILTINS = new HashSet<>(Arrays.asList(
            "showerr", "cd", "set", "unset", "enable",
            "login", "logout", "help", "version", "alias",
            "unalias", "exit"
    ));


    public static final String ENV_PATH = "PATH";
    public static final String ENV_HOME = "HOME";
    private static final Logger LOG = Logger.getLogger(Nsh.class.getName());
    private final NshOptions options;
    private final NshHistory history;
    private final List<NshVarListener> listeners = new ArrayList<>();
    protected NshContext rootContext;
    private NClock bootStartMillis;
    private NshEvaluator evaluator;
    private NshErrorHandler errorHandler;
    private NshExternalExecutor externalExecutor;
    private NshCommandTypeResolver commandTypeResolver;
    private NId appId = null;
    private String serviceName = null;
    private Supplier<NMsg> headerMessageSupplier = null;
    private NshConfig configuration;

    public Nsh() {
        this(new NshConfig());
    }

    public Nsh(NshConfig configuration) {
        if (configuration == null) {
            configuration = new NshConfig();
        } else {
            configuration = configuration.copy();
        }
        this.configuration = configuration;
        headerMessageSupplier = configuration.getHeaderMessageSupplier();
        serviceName = configuration.getServiceName();
        String[] args = resolveArgs(configuration.getArgs());
        NshOptionsParser nshOptionsParser = configuration.getOptionsParser();
        NshEvaluator evaluator = configuration.getEvaluator();
        NshCommandTypeResolver commandTypeResolver = configuration.getCommandTypeResolver();
        NshErrorHandler errorHandler = configuration.getErrorHandler();
        NshExternalExecutor externalExecutor = configuration.getExternalExecutor();
        NId appId = configuration.getAppId();

        this.appId = appId;
        this.bootStartMillis = NApp.of().getStartTime();
        //super.setCwd(workspace.getConfigManager().getCwd());
        if (this.appId == null) {
            this.appId = NApp.of().getId().orNull();
            if (this.appId == null) {
                this.appId = NId.getForClass(Nsh.class).orNull();
            }
        }
        if (this.appId == null) {
            throw new IllegalArgumentException("unable to resolve application id");
        }
        if (serviceName == null) {
            serviceName = this.appId.getArtifactId();
        }

        serviceName = resolveServiceName(serviceName, appId);
        if (commandTypeResolver == null) {
            this.commandTypeResolver = new NCommandTypeResolver();
//            this.commandTypeResolver = new DefaultNshCommandTypeResolver();
        } else {
            this.commandTypeResolver = commandTypeResolver;
        }
        if (errorHandler == null) {
            this.errorHandler = new DefaultErrorHandler();
        } else {
            this.errorHandler = errorHandler;
        }
        if (evaluator == null) {
            this.evaluator = new DefaultNshEvaluator();
//            this.evaluator = new NshEvaluatorBase();
        } else {
            this.evaluator = evaluator;
        }
        Boolean includeHistory = configuration.getIncludeHistory();
        if (includeHistory == null) {
            includeHistory = true;
        }
        if(includeHistory) {
            NshHistory history = configuration.getHistory();
            if (history == null) {
                this.history = new DefaultNshHistory();
            } else {
                this.history = history;
            }
        }else{
            this.history = new NoHistory();
        }
        if (nshOptionsParser == null) {
            nshOptionsParser = new DefaultNshOptionsParser();
        }
        this.options = nshOptionsParser.parse(args);
        if (externalExecutor == null) {
            boolean includeExternalExecutor = configuration.getIncludeExternalExecutor() != null && configuration.getIncludeExternalExecutor();
            if (includeExternalExecutor) {
                this.externalExecutor = new NshToNutsExternalExecutor();
//                this.externalExecutor = new NshNoExternalExecutor();
            }
        } else {
            this.externalExecutor = externalExecutor;
        }
        if (options.getServiceName() == null) {
            options.setServiceName(serviceName == null ? "nsh" : serviceName);
        }

        NshContext _rootContext = getRootContext();

        NWorkspace.of().setProperty(NshContext.class.getName(), _rootContext);
        _rootContext.setSession(NSession.of());
        //add default commands
        List<NshBuiltin> allCommand = new ArrayList<>();
        NScorableContext constraints = NScorableContext.of(this);

        Predicate<NshBuiltin> filter = new NshBuiltinPredicate(configuration);
        for (NshBuiltin command : NWorkspace.of().extensions()
                .createServiceLoader(NshBuiltin.class, Nsh.class, NshBuiltin.class.getClassLoader())
                .loadAll(this)) {
            NshBuiltin old = _rootContext.builtins().find(command.getName());
            if (old != null && old.getScore(constraints) >= command.getScore(constraints)) {
                continue;
            }
            if (filter.test(command)) {
                allCommand.add(command);
            }
        }
        _rootContext.builtins().set(allCommand.toArray(new NshBuiltin[0]));
        _rootContext.getUserProperties().put(NshContext.class.getName(), _rootContext);

        try {
            NPath histFile = this.history.getHistoryFile();
            if (histFile == null) {
                histFile = NPath.ofIdStore(this.appId, NStoreType.VAR).resolve((serviceName == null ? "" : serviceName) + ".history");
                this.history.setHistoryFile(histFile);
                if (histFile.exists()) {
                    this.history.load(histFile);
                }
            }
        } catch (Exception ex) {
            NLog.of(Nsh.class)
                    .log(NMsg.ofC("error resolving history file %s", this.history.getHistoryFile()).asError(ex));
        }
        NWorkspace.of().setProperty(NshHistory.class.getName(), this.history);

    }

    private static String[] resolveArgs(String[] args) {
        if (args != null) {
            return args;
        }
        return NApp.of().getArguments().toArray(new String[0]);
    }

    private static String resolveServiceName(String serviceName, NId appId) {
        if ((serviceName == null || serviceName.trim().isEmpty())) {
            if (appId == null) {
                appId = NId.getForClass(Nsh.class).get();
            }
            serviceName = appId.getArtifactId();
        }
        return serviceName;
    }

    public static void uninstallFromNuts() {
        NLog log = NLog.of(Nsh.class);
        log.log(NMsg.ofPlain("[nsh] uninstallation...").withLevel(Level.CONFIG).withIntent(NMsgIntent.NOTICE));
        try {
            try {
                NWorkspace.of().removeCommandFactory("nsh");
            } catch (Exception notFound) {
                //ignore!
            }
            Set<String> uninstalled = new TreeSet<>();
            for (NCustomCmd command : NWorkspace.of().findCommandsByOwner(NApp.of().getId().orNull())) {
                try {
                    NWorkspace.of().removeCommand(command.getName());
                    uninstalled.add(command.getName());
                } catch (Exception ex) {
                    if (NSession.of().isPlainTrace()) {
                        NTexts factory = NTexts.of();
                        NSession.of().err().println(NMsg.ofC("unable to unregister %s.",
                                factory.ofStyled(command.getName(), NTextStyle.primary3())
                        ));
                    }
                }
            }
            if (!uninstalled.isEmpty()) {
                log.log(NMsg.ofC("[nsh] unregistered %s nsh commands : %s", uninstalled.size(),
                        String.join(", ", uninstalled))
                        .withLevel(Level.CONFIG).withIntent(NMsgIntent.NOTICE)
                );
            }
        } catch (Exception ex) {
            //ignore
        }
    }

    public void addVarListener(NshVarListener listener) {
        this.listeners.add(listener);
    }

    public void removeVarListener(NshVarListener listener) {
        this.listeners.add(listener);
    }

    public NshVarListener[] getVarListeners() {
        return listeners.toArray(new NshVarListener[0]);
    }

    public NshEvaluator getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(NshEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public NshCommandTypeResolver getCommandTypeResolver() {
        return commandTypeResolver;
    }

    public void setCommandTypeResolver(NshCommandTypeResolver whichResolver) {
        this.commandTypeResolver = whichResolver;
    }

    public NshExternalExecutor getExternalExecutor() {
        return externalExecutor;
    }

    public void setExternalExecutor(NshExternalExecutor externalExecutor) {
        this.externalExecutor = externalExecutor;
    }

    public NshErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(NshErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public List<String> findFiles(final String namePattern, boolean exact, String parent) {
        if (exact) {
            String[] all = NPath.of(parent).stream()
                    .filter(NPredicate.of((NPath x) -> namePattern.equals(x.getName())).redescribe(NElementDescribables.ofDesc("name='" + namePattern + "'")))
                    .map(NFunction.of(NPath::toString).redescribe(NElementDescribables.ofDesc("toString"))).toArray(String[]::new);
            return Arrays.asList(all);
        } else {
            final Pattern o = Pattern.compile(namePattern);
            String[] all = NPath.of(parent).stream()
                    .filter(NPredicate.of((NPath x) -> o.matcher(x.getName()).matches()).redescribe(NElementDescribables.ofDesc("name~~'" + namePattern + "'")))
                    .map(NFunction.of(NPath::toString).redescribe(NElementDescribables.ofDesc("toString"))).toArray(String[]::new);
            return Arrays.asList(all);
        }
    }

    protected NshContext createRootContext(String serviceName, String[] args) {
        return createContext(null, null, null, null, serviceName, args);
    }

    public NshContext createNewContext(NshContext parentContext) {
        return createNewContext(parentContext, parentContext.getServiceName(), parentContext.getArgsArray());
    }

    public NshContext createNewContext(NshContext ctx, String serviceName, String[] args) {
        return createContext(ctx, null, null, null, serviceName, args);
    }

    public NshContext createInlineContext(NshContext ctx, String serviceName, String[] args) {
        if (ctx == null) {
            ctx = getRootContext();
        }
        NshContextForSource c = new NshContextForSource(ctx);
        c.setServiceName(serviceName);
        c.setArgs(args);
        return c;
    }

    public NshCommandNode createCommandNode(String[] args) {
        return NshParser.createCommandNode(args);
    }

    public NshContext getRootContext() {
        if (rootContext == null) {
            rootContext = createRootContext(options.getServiceName(), options.getCommandArgs().toArray(new String[0]));
        }
        return rootContext;
    }

    public void executeLine(String line, boolean storeResult, NshContext context) {
        if (context == null) {
            context = getRootContext();
        }
        boolean success = false;
        if (line.trim().length() > 0 && !line.trim().startsWith("#")) {
            try {
                getHistory().add(line);
                NshCommandNode nn = parseScript(line);
                int i = context.nsh().evalNode(nn, context);
                success = i == 0;
            } catch (NshQuitException e) {
                throw e;
            } catch (Throwable e) {
                if (storeResult) {
                    onResult(e, context);
                } else {
                    if (e instanceof RuntimeException) {
                        throw e;
                    }
                    if (e instanceof Error) {
                        throw e;
                    }
                    throw new RuntimeException(e);
                }
            }
            if (storeResult) {
                if (success) {
                    onResult(null, context);
                    try {
                        history.save();
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                }
            }
        }
    }

    public int onResult(int r, NshContext context) {
        context.setLastResult(new NshResult(r, null, null));
        return r;
    }

    public int onResult(Throwable th, NshContext context) {
        if (th == null) {
            context.setLastResult(new NshResult(0, null, null));
            return 0;
        }
        if (th instanceof NshQuitException) {
            throw (NshQuitException) th;
        }
        if (getErrorHandler().isQuitException(th)) {
            if (th instanceof RuntimeException) {
                throw (RuntimeException) th;
            }
            throw new NshQuitException(th, 100);
        }

        if (th instanceof NshException) {
            NshException je = (NshException) th;
            int errorCode = je.getExitCode();
            String lastErrorMessage = getErrorHandler().errorToMessage(th);
            context.setLastResult(new NshResult(errorCode, lastErrorMessage, th));
            if (errorCode != NExecutionException.SUCCESS) {
                getErrorHandler().onError(lastErrorMessage, th, context);
            }
            return errorCode;
        }

        int errorCode = getErrorHandler().errorToCode(th);
        String lastErrorMessage = getErrorHandler().errorToMessage(th);
        context.setLastResult(new NshResult(errorCode, lastErrorMessage, th));
        if (errorCode != 0) {
            getErrorHandler().onError(lastErrorMessage, th, context);
        }
        return errorCode;
    }

    public int onResult(int errorCode, Throwable th, NshContext context) {
        if (errorCode != 0) {
            if (th == null) {
                th = new RuntimeException("error occurred. Error Code #" + errorCode);
            }
        } else {
            th = null;
        }
        String lastErrorMessage = th == null ? null : getErrorHandler().errorToMessage(th);
        context.setLastResult(new NshResult(errorCode, lastErrorMessage, th));
        if (errorCode != 0) {
            getErrorHandler().onError(lastErrorMessage, th, context);
        }
        return errorCode;
    }

    public int executeCommand(String[] command, NshContext context) {
        context.setServiceName(command[0]);
        context.setArgs(Arrays.copyOfRange(command, 1, command.length));
        return context.nsh().evalNode(createCommandNode(command), context);
    }

    public void addToHistory(String[] command) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < command.length; i++) {
            String arg = command[i];
            if (i > 0) {
                sb.append(" ");
            }
            if (arg.contains(" ")) {
                sb.append("\"").append(arg).append("\"");
            } else {
                sb.append(arg);
            }
        }
        getHistory().add(sb.toString());
    }

    public int executePreparedCommand(String[] command,
                                      boolean considerAliases, boolean considerBuiltins, boolean considerExternal,
                                      NshContext context
    ) {
        context.nsh().traceExecution(() -> String.join(" ", command), context);
        String cmdToken = command[0];
        NPath cmdPath = NPath.of(cmdToken);
        if (!cmdPath.isName()) {
            if (isScriptFile(cmdPath)) {
                return executeServiceFile(createNewContext(context, cmdPath.toString(), command), false);
            } else {
                final NshExternalExecutor externalExec = getExternalExecutor();
                if (externalExec == null) {
                    throw new NshException(NMsg.ofC("not found %s", cmdToken), 101);
                }
                return externalExec.execExternalCommand(command, context);
            }
        } else {
            List<String> cmds = new ArrayList<>(Arrays.asList(command));
            String a = considerAliases ? context.aliases().get(cmdToken) : null;
            if (a != null) {
                NshNode node0 = null;
                try {
//                    NshParser parser = new NshParser();
//                    node0 = parser.parse(a);

                    node0 = NshParser.fromString(a).parse();

                } catch (Exception ex) {
                    Logger.getLogger(Nsh.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (node0 instanceof NshCmdLineNode) {
                    NshCmdLineNode nn = (NshCmdLineNode) node0;
                    List<String> newCmd = new ArrayList<>();
                    for (NshArgumentNode item : nn) {
                        newCmd.addAll(Arrays.asList(item.evalString(context)));
                    }
                    for (int i = 1; i < cmds.size(); i++) {
                        newCmd.add(cmds.get(i));
                    }
                    cmds.clear();
                    cmds.addAll(newCmd);
                } else {
                    throw new IllegalArgumentException("invalid  alias " + a);
                }
            } else {
                a = cmdToken;
            }
            NshBuiltin nshCommand = considerBuiltins ? context.builtins().find(a) : null;
            if (nshCommand != null && nshCommand.isEnabled()) {
                ArrayList<String> arg2 = new ArrayList<String>(cmds);
                arg2.remove(0);
                nshCommand.exec(arg2.toArray(new String[0]), context.createCommandContext(nshCommand));
            } else {
                if (considerExternal) {
                    final NshExternalExecutor externalExec = getExternalExecutor();
                    if (externalExec == null) {
                        throw new NshException(NMsg.ofC("not found %s", cmdToken), 101);
                    }
                    externalExec.execExternalCommand(cmds.toArray(new String[0]), context);
                } else {
                    throw new NshException(NMsg.ofC("not found %s", cmdToken), 101);
                }
            }
        }
        return 0;
    }

    private boolean isScriptFile(NPath cmdPath) {
        if (cmdPath.getName().endsWith(".nsh")) {
            return true;
        }
        if (cmdPath.getName().endsWith(".sh")) {
            return true;
        }
        if (cmdPath.exists()) {
            String firstLine = cmdPath.lines().findFirst().orElse(null);
            if (firstLine != null) {
                if (firstLine.startsWith("#!/bin/sh")) {
                    return true;
                }
                if (firstLine.startsWith("#!/bin/bash")) {
                    return true;
                }
                if (firstLine.startsWith("#!/bin/nsh")) {
                    return true;
                }
            }
        }
        return false;
    }

    public void run() {
        try {
            if (NApp.of().getAutoComplete() != null) {
                return;
            }
            NshContext rootContext = getRootContext();
            if (getOptions().isHelp()) {
                executeHelp(rootContext);
                return;
            }
            if (getOptions().isVersion()) {
                executeVersion(rootContext);
                return;
            }
            String[] commandArgs = getOptions().getCommandArgs().toArray(new String[0]);
            if (getOptions().isCommand()) {
                if (commandArgs.length == 0) {
                    //
                } else if (commandArgs.length == 1) {
                    executeServiceStream(rootContext, "command", new ByteArrayInputStream(commandArgs[0].getBytes()));
                } else {
                    executeServiceStream(rootContext, "command", new ByteArrayInputStream(
                            NCmdLine.of(commandArgs).toString().getBytes()
                    ));
                }
                //executeCommand(commandArgs, rootContext);
            }
            if (getOptions().isReadCommandsFromStdIn()) {
                int r = executeServiceStream(rootContext, "in", rootContext.in());
                if (r == NExecutionException.SUCCESS) {
                    return;
                }
                onQuit(new NshQuitException(r));
                return;
            }

            if (!getOptions().getFiles().isEmpty()) {
                for (String file : getOptions().getFiles()) {
                    executeServiceFile(createNewContext(rootContext, file, commandArgs), false);
                }
            }
            if (getOptions().isInteractive() || (commandArgs.length == 0 && getOptions().getFiles().isEmpty())) {
                executeInteractive(rootContext);
            }
        } catch (NExecutionException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NExecutionException(NMsg.ofC("%s", ex), ex, NExecutionException.ERROR_1);
        }
    }

    protected String readInteractiveLine(NshContext context) {
        NTerminal terminal = context.getSession().getTerminal();
        return terminal.readLine(getPromptString(context));
    }

    protected void printHeader(NPrintStream out) {
        NMsg m = null;
        if (headerMessageSupplier != null) {
            m = headerMessageSupplier.get();
            if (m == null) {
                return;
            }
        }
        if (m == null) {
            NDescriptor resultDescriptor = null;
            if (appId != null) {
                try {
                    resultDescriptor = NFetchCmd.of(appId)
                            .setDependencyFilter(NDependencyFilters.of().byRunnable())
                            .getResultDescriptor();
                } catch (Exception ex) {
                    //just ignore
                }
            }
            NDescriptorContributor contributor = null;
            if (resultDescriptor != null) {
                for (NDescriptorContributor c : resultDescriptor.getDevelopers()) {
                    contributor = c;
                    break;
                }
            }
            String copyRight = null;
            if (resultDescriptor != null && resultDescriptor.getLicenses() != null) {
                for (NDescriptorLicense license : resultDescriptor.getLicenses()) {
                    if (!NBlankable.isBlank(license.getDate())) {
                        copyRight = license.getDate();
                        break;
                    }
                }
            }
            if (resultDescriptor != null && resultDescriptor.getLicenses() != null) {
                for (NDescriptorLicense license : resultDescriptor.getLicenses()) {
                    if (!NBlankable.isBlank(license.getName())) {
                        copyRight = license.getName();
                        break;
                    }
                }
            }
            if (NBlankable.isBlank(copyRight)) {
                copyRight = String.valueOf(Year.now().getValue());
            }
            m = NMsg.ofC("%s v%s (c) %s",
                    NMsg.ofStyledPrimary1(NStringUtils.firstNonNull(serviceName, "app")),
                    (appId == null || appId.getVersion().isBlank()) ?
                            getRootContext().getWorkspace().getRuntimeId().getVersion() :
                            appId.getVersion()
                    , contributor == null ? "thevpc" : NStringUtils.firstNonBlank(
                            contributor.getName(),
                            contributor.getEmail(),
                            contributor.getId()
                    ),
                    copyRight
            );
        }
        out.resetLine().println(m);
    }

    protected void executeHelp(NshContext context) {
        context.out().println("Syntax : nsh [<FILE>]\n");
        context.out().println("    <FILE> : if present content will be processed as input\n");
    }

    protected void executeVersion(NshContext context) {
        context.out().println(NApp.of().getId().get().getVersion());
    }

    protected void executeInteractive(NshContext context) {
        NSystemTerminal.enableRichTerm();
        NPath appVarFolder = NApp.of().getVarFolder();
        if (appVarFolder == null) {
            appVarFolder = NPath.ofIdStore(
                    NId.get("net.thevpc.nsh:nsh").get()
                    , NStoreType.VAR);
        }
        NIO.of().getSystemTerminal()
                .setCommandAutoCompleteResolver(new NshAutoCompleter())
                .setCommandHistory(
                        NCmdLineHistory.of()
                                .setPath(appVarFolder.resolve("nsh-history.hist"))
                );
        prepareContext(getRootContext());
        printHeader(context.out());
        if (getOptions().isLogin()) {
            executeLoginScripts();
        }

        while (true) {
            String line = null;
            try {
                line = readInteractiveLine(context);
            } catch (Exception ex) {
                onResult(ex, context);
                break;
            }
            if (line == null) {
                break;
            }
            if (line.trim().length() > 0) {
                try {
                    executeLine(line, true, context);
                } catch (NshQuitException q) {
                    if (getOptions().isLogin()) {
                        executeLogoutScripts();
                    }
                    if (q.getExitCode() == NExecutionException.SUCCESS) {
                        return;
                    }
                    onQuit(q);
                    return;
                }
            }
        }
        if (getOptions().isLogin()) {
            executeLogoutScripts();
        }
        onQuit(new NshQuitException(0));
    }

    private void executeLoginScripts() {
        if (!getOptions().isNoProfile()) {
            for (String profileFile : new String[]{
                    "/etc/profile",
                    (getOptions().isPosix()) ? null : "~/.bash_profile",
                    (getOptions().isPosix()) ? null : "~/.bash_login",
                    "~/.profile",
                    getOptions().isBash() || getOptions().isPosix() ? null : getOptions().getStartupScript()
            }) {
                if (profileFile != null) {
                    if (profileFile.startsWith("~/") || profileFile.startsWith("~\\")) {
                        profileFile = System.getProperty("user.home") + profileFile.substring(1);
                    }
                    executeServiceFile(createNewContext(getRootContext(), profileFile, new String[0]), true);
                }
            }
        }
    }

    private void executeLogoutScripts() {
        if (!getOptions().isNoProfile()) {
            for (String profileFile : new String[]{
                    (getOptions().isPosix()) ? null : "~/.bash_logout",
                    (getOptions().isBash() || getOptions().isPosix()) ? null : getOptions().getStartupScript()
            }) {
                if (profileFile != null) {
                    if (profileFile.startsWith("~/") || profileFile.startsWith("~\\")) {
                        profileFile = System.getProperty("user.home") + profileFile.substring(1);
                    }
                    executeServiceFile(createNewContext(getRootContext(), profileFile, new String[0]), true);
                }
            }
        }
    }

    protected void onQuit(NshQuitException quitException) {
        getHistory().save();
        if (quitException.getExitCode() == 0) {
            return;
        }
        throw new NExecutionException(NMsg.ofC("%s", quitException), quitException.getExitCode());
//        throw quitException;
    }

    public int executeServiceFile(NshContext context, boolean ignoreIfNotFound) {
        String file = context.getServiceName();
        if (file != null) {
            file = NPath.of(file).toAbsolute(context.getDirectory()).toString();
        }
        if (file == null || !NPath.of(file).exists()) {
            if (ignoreIfNotFound) {
                return 0;
            }
            throw new NshException(NMsg.ofC("file not found : %s", file), 1);
        }
        try (InputStream stream = NPath.of(file).getInputStream()) {
            return executeServiceStream(context, file, stream);
        } catch (IOException ex) {
            throw new NshException(ex, 1);
        }
    }

    public int executeServiceStream(NshContext context, String serviceName, InputStream stream) {
        context.setServiceName(serviceName);
        NshCommandNode ii = parseScript(stream);
        if (ii == null) {
            return 0;
        }
        NshContext c = context.setRootNode(ii);//.setParent(null);
        return context.nsh().evalNode(ii, c);
    }

    public int executeScript(String text, NshContext context) {
        if (context == null) {
            context = getRootContext();
        }
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        NshCommandNode ii = parseScript(text);
        if (ii == null) {
            return 0;
        }
        NshContext c = context.setRootNode(ii);//.setParent(null);
        return evalNode(ii, c);
    }

    public int evalNode(NshCommandNode node, NshContext context) {
        try {
            int r = node.eval(context);
            onResult(r, context);
            return r;
        } catch (NshUniformException th) {
            if (th.isQuit()) {
                onResult(null, context);
                th.throwQuit();
                return 0;
            } else {
                onResult(th, context);
                throw th;
            }
        } catch (NshQuitException th) {
            throw th;
        } catch (Exception th) {
            if (getErrorHandler().isQuitException(th)) {
                onResult(null, context);
                throw new NshUniformException(getErrorHandler().errorToCode(th), true, th);
            }
            onResult(th, context);
            context.err().println(NMsg.ofC("error: %s", th));
            return getErrorHandler().errorToCode(th);
        }
    }

    public int safeEval(NshCommandNode n, NshContext context) {
        boolean success = false;
        try {
            n.eval(context);
            success = true;
        } catch (Exception ex2) {
            return onResult(ex2, context);
        }
        if (success) {
            return onResult(null, context);
        }
        throw new IllegalArgumentException("Unexpected behaviour");
    }

    //    public String getPromptString() {
//        return getPromptString(getRootContext());
//    }
    protected NMsg getPromptString(NshContext context) {
        NSession session = context.getSession();
//        String wss = ws == null ? "" : new File(getRootContext().getAbsolutePath(ws.config().getWorkspaceLocation().toString())).getName();
        String login = null;
        if (session != null) {
            login = NWorkspaceSecurityManager.of().getCurrentUsername();
        }
        String prompt = ((login != null && login.length() > 0 && !"anonymous".equals(login)) ? (login + "@") : "");
        if (!NBlankable.isBlank(getRootContext().getServiceName())) {
            prompt = prompt + getRootContext().getServiceName();
        }
        prompt += "> ";
        return NMsg.ofPlain(prompt);
    }

    protected String getPromptString0(NshContext context) {

        String promptValue = context.vars().getAll().getProperty("PS1");
        if (promptValue == null) {
            promptValue = "\\u> ";
        }
        char[] promptChars = promptValue.toCharArray();
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < promptChars.length; i++) {
            char c = promptChars[i];
            if (c == '\\' && i < (promptChars.length - 1)) {
                i++;
                c = promptChars[i];
                switch (c) {
                    case 'W': {
                        s.append(context.getDirectory());
                        break;
                    }
                    case 'u': {
                        s.append(context.vars().getAll().getProperty("USER", "anonymous"));
                        break;
                    }
                    case 'h': {
                        String h = context.vars().getAll().getProperty("HOST", "nowhere");
                        if (h.contains(".")) {
                            h = h.substring(0, h.indexOf('.'));
                        }
                        s.append(h);
                        break;
                    }
                    case 'H': {
                        s.append(context.vars().getAll().getProperty("HOST", "nowhere"));
                        break;
                    }
                    default: {
                        s.append('\\').append(c);
                        break;
                    }
                }
            } else {
                s.append(c);
            }
        }
        return s.toString();

    }

    //    public String evalAsString(String param, NshContext context) {
//        Properties envs = new Properties();
//        Properties processEnvs = context.vars().getAll();
//        for (Entry<Object, Object> entry : processEnvs.entrySet()) {
//            envs.put(entry.getKey(), entry.getValue());
//        }
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < param.length(); i++) {
//            char c = param.charAt(i);
//            if (c == '$') {
//                StringBuilder var = new StringBuilder();
//                i++;
//                if (i < param.length()) {
//                    if (param.charAt(i) != '{') {
//                        while (i < param.length()
//                                && ((param.charAt(i) >= 'a' && param.charAt(i) <= 'z')
//                                || (param.charAt(i) >= 'A' && param.charAt(i) <= 'Z')
//                                || (param.charAt(i) >= 'O' && param.charAt(i) <= '9')
//                                || (param.charAt(i) == '_'))) {
//                            var.append(param.charAt(i++));
//                        }
//                        i--;
//                    } else {
//                        i++;//ignore '{'
//                        while (i < param.length() && (param.charAt(i) != '}')) {
//                            var.append(param.charAt(i++));
//                        }
//                    }
//                } else {
//                    var.append('$');
//                }
//                Object obj = envs.get(var.toString());
//                sb.append(obj == null ? "" : String.valueOf(obj));
//            } else {
//                sb.append(c);
//            }
//        }
//        return sb.toString();
//    }
//
//    public String[] findExecFilesInPath(String filePath, String[] classNames, NshContext context) {
//        ArrayList<String> found = new ArrayList<String>();
//        File f = new File(filePath);
//        if (!f.exists()) {
//            return new String[0];
//        }
//        if (f.isDirectory()) {
//            for (String ff : classNames) {
//                File f2 = new File(f, ff);
//                if (f2.exists()) {
//                    found.add(f2.getPath());
//                }
//            }
//        }
//        return found.toArray(new String[found.size()]);
//    }
//
//    public String[] findClassesInPath(String filePath, String[] classNames, NshContext context) {
//        System.out.printf("findClassesInPath : path=%s should contain? %s\n", filePath, Arrays.asList(classNames).toString());
//        ArrayList<String> found = new ArrayList<String>();
//        String[] expanded = context.expandPaths(filePath/*, null*/);
//        System.out.printf("path=%s expanded to %s\n", filePath, Arrays.asList(expanded));
//        for (String fp : expanded) {
//            System.out.printf("\tfindClassesInPath : path=%s should contain? %s\n", fp, Arrays.asList(classNames));
//            File f = new File(fp);
//            if (f.exists()) {
//                String[] fileCls = new String[classNames.length];
//                for (int i = 0; i < fileCls.length; i++) {
//                    fileCls[i] = classNames[i].replace('.', '/') + ".class";
//
//                }
//                List<String> clsNames = Arrays.asList(fileCls);
//                if (f.isDirectory()) {
//                    for (String ff : fileCls) {
//                        if (new File(f, ff).exists()) {
//                            found.add(ff);
//                        }
//                    }
//                } else {
//                    ZipFile zipFile = null;
//                    boolean fileFound = false;
//                    try {
//                        System.out.printf("lookup into %s for %s\n", fp, clsNames);
//                        // open a zip file for reading
//                        zipFile = new ZipFile(fp);
//                        // get an enumeration of the ZIP file entries
//                        Enumeration<? extends ZipEntry> e = zipFile.entries();
//                        while (e.hasMoreElements()) {
//                            ZipEntry entry = e.nextElement();
//                            String entryName = entry.getName();
//                            for (String ff : fileCls) {
//                                if (entryName.equals(ff)) {
//                                    found.add(ff);
//                                    break;
//                                }
//                            }
//                            if (found.size() == classNames.length) {
//                                break;
//                            }
//                        }
//
//                    } catch (IOException ioe) {
//                        //return found;
//                    } finally {
//                        try {
//                            if (zipFile != null) {
//                                zipFile.close();
//                            }
//                        } catch (IOException ioe) {
//                            System.err.printf("Error while closing zip file %s\n", ioe);
//                        }
//                    }
//                }
//            }
//        }
//        return found.toArray(new String[found.size()]);
//    }
    public void prepareContext(NshContext context) {
//        try {
//            cwd = new File(".").getCanonicalPath();
//        } catch (IOException ex) {
//            cwd = new File(".").getAbsolutePath();
//        }
        context.vars().set(NWorkspace.of().getSysEnv());
        setUndefinedStartupEnv("USER", System.getProperty("user.name"), context);
        setUndefinedStartupEnv("LOGNAME", System.getProperty("user.name"), context);
        setUndefinedStartupEnv(Nsh.ENV_PATH, ".", context);
        setUndefinedStartupEnv("PWD", System.getProperty("user.dir"), context);
        setUndefinedStartupEnv(Nsh.ENV_HOME, System.getProperty("user.home"), context);
        setUndefinedStartupEnv("PS1", ">", context);
        setUndefinedStartupEnv("IFS", " \t\n", context);
    }

    private void setUndefinedStartupEnv(String name, String defaultValue, NshContext context) {
        if (context.vars().get(name) == null) {
            context.vars().set(name, defaultValue);
        }
    }

    public NshScript parseScript(InputStream stream) {
        NshNode node0 = null;
        try {
            node0 = NshParser.fromInputStream(stream).parse();
            if (node0 == null) {
                return null;
            }
        } catch (Exception ex) {
            Logger.getLogger(Nsh.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (node0 instanceof NshCommandNode) {
            return new NshScript((NshCommandNode) node0);
        }
        throw new IllegalArgumentException("expected node " + node0);
    }

    public NshScript parseScript(String scriptString) {
        NshNode node0 = null;
        try {
            node0 = NshParser.fromString(scriptString).parse();
            if (node0 == null) {
                return null;
            }
        } catch (Exception ex) {
            Logger.getLogger(Nsh.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (node0 instanceof NshCommandNode) {
            return new NshScript((NshCommandNode) node0);
        }
        throw new IllegalArgumentException("expected node " + scriptString);
    }
//    public String escapeStringForDoubleQuotes(String s) {
//        StringBuilder sb=new StringBuilder();
//        for (char c: s.toCharArray()) {
//            switch (c){
//                case '\\':
//                case '(':
//                case ')':
//                case '&':
//                case '|':
//                    {
//                    sb.append('\\');
//                    sb.append(c);
//                    break;
//                }
//                default:{
//                    sb.append(c);
//                }
//            }
//        }
//        return sb.toString();
//    }

    public String escapeString(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\':
                case '&':
                case '!':
                case '$':
                case '`':
                case '?':
                case '*':
                case '[':
                case ']': {
                    sb.append('\\');
                    sb.append(c);
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public String escapePath(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '?':
                case '*':
                case '[':
                case ']': {
                    sb.append('\\');
                    sb.append(c);
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public void traceExecution(Supplier<String> msg, NshContext context) {
        if (getOptions().isXtrace()) {
            String txt = msg.get();
            context.err().println("+ " + txt);
        }
    }

    public NshOptions getOptions() {
        return options;
    }

    public NshHistory getHistory() {
        return history;
    }

    public String getVersion() {
        NId nutsId = NId.getForClass(getClass()).orNull();
        if (nutsId == null) {
            return "dev";
        }
        return nutsId.getVersion().getValue();
    }

    public MemResult executeCommand(String[] command) {
        return executeCommand(command, (String) null);
    }

    public MemResult executeCommand(String[] command, String in) {
        StringBuilder out = new StringBuilder();
        StringBuilder err = new StringBuilder();
        ByteArrayPrintStream oout = new ByteArrayPrintStream();
        ByteArrayPrintStream oerr = new ByteArrayPrintStream();
        NshContext newContext = createNewContext(getRootContext(), command[0], Arrays.copyOfRange(command, 1, command.length));
        newContext.setIn(new ByteArrayInputStream(in == null ? new byte[0] : in.getBytes()));
        newContext.setOut(oout);
        newContext.setErr(oerr);
        int r = executeCommand(command, newContext);
        out.append(oout);
        err.append(oerr);
        return new MemResult(out.toString(), err.toString(), r);
    }

    public NshContext createContext(NshContext ctx, NshNode root, NshNode parent, NshVariables env, String serviceName, String[] args) {
        return new DefaultNshContext(this, root, parent, ctx, env, serviceName, args);
    }

    public void installToNuts() {
        NSession session = NSession.of();
        NLog log = NLog.of(Nsh.class);
        if (session.isTrace() || session.isYes()) {
            log.log(NMsg.ofC("[nsh] activating options trace=%s yes=%s", session.isTrace(), session.isYes())
                    .withLevel(Level.CONFIG).withIntent(NMsgIntent.NOTICE)
            );
        }
        String nshIdStr = NApp.of().getId().get().getShortName();
        NshBuiltin[] commands = getRootContext().builtins().getAll();
        Set<String> reinstalled = new TreeSet<>();
        Set<String> firstInstalled = new TreeSet<>();
        for (NshBuiltin command : commands) {
            if (!CONTEXTUAL_BUILTINS.contains(command.getName())) {
                // avoid recursive definition!
                // disable trace, summary will be traced later!
                if (session.getWorkspace()
                        .addCommand(new NCommandConfig()
                                .setFactoryId("nsh")
                                .setName(command.getName())
                                .setCommand(nshIdStr, "-c", command.getName())
                                .setOwner(NApp.of().getId().orNull())
                                .setHelpCommand(nshIdStr, "-c", "help", "--ntf", command.getName())
                        )) {
                    reinstalled.add(command.getName());
                } else {
                    firstInstalled.add(command.getName());
                }
            }
        }

        if (!firstInstalled.isEmpty()) {
            log.log(NMsg.ofC("[nsh] registered %s nsh commands : %s", firstInstalled.size(),
                    String.join(", ", firstInstalled))
                    .withLevel(Level.CONFIG).withIntent(NMsgIntent.NOTICE)
            );
        }
        if (!reinstalled.isEmpty()) {
            log.log(NMsg.ofC("[nsh] re-registered %s nsh commands : %s", reinstalled.size(),
                    String.join(", ", reinstalled))
                    .withLevel(Level.CONFIG).withIntent(NMsgIntent.NOTICE)
            );
        }
        if (session.isPlainTrace()) {
            NTexts factory = NTexts.of();
            if (!firstInstalled.isEmpty()) {
                NOut.println(NMsg.ofC("registered %s nsh commands : %s",
                        factory.ofStyled("" + firstInstalled.size(), NTextStyle.primary3()),
                        factory.ofStyled(String.join(", ", firstInstalled), NTextStyle.primary3())
                ));
            }
            if (!reinstalled.isEmpty()) {
                NOut.println(NMsg.ofC("re-registered %s nsh commands : %s",
                        factory.ofStyled("" + reinstalled.size(), NTextStyle.primary3()),
                        factory.ofStyled(String.join(", ", reinstalled), NTextStyle.primary3())
                ));
            }
        }
        if (NWorkspace.of().getBootOptions().getInitScripts()
                .ifEmpty(true)
                .orElse(false)) {
            boolean initLaunchers = NWorkspace.of().getBootOptions().getInitLaunchers()
                    .ifEmpty(true)
                    .orElse(false);
            NWorkspace.of().addLauncher(
                    new NLauncherOptions()
                            .setId(NApp.of().getId().orNull())
                            .setCreateScript(true)
                            .setCreateDesktopLauncher(initLaunchers ? NSupportMode.PREFERRED : NSupportMode.NEVER)
                            .setCreateMenuLauncher(initLaunchers ? NSupportMode.SUPPORTED : NSupportMode.NEVER)
                            .setOpenTerminal(true)
            );
        }
        session.getWorkspace().saveConfig(false);
    }

    private static class NshBuiltinPredicate implements Predicate<NshBuiltin> {
        private final NshConfig configuration;

        boolean includeCoreBuiltins;
        boolean includeDefaultBuiltins;

        public NshBuiltinPredicate(NshConfig configuration) {
            this.configuration = configuration;
            includeCoreBuiltins = configuration.getIncludeCoreBuiltins() == null || configuration.getIncludeCoreBuiltins();
            includeDefaultBuiltins = configuration.getIncludeDefaultBuiltins() != null && configuration.getIncludeDefaultBuiltins();
        }

        @Override
        public boolean test(NshBuiltin nshBuiltin) {
            if (!includeCoreBuiltins) {
                if (nshBuiltin instanceof NshBuiltinCore) {
                    return false;
                }
            }
            if (!includeDefaultBuiltins) {
                if (nshBuiltin instanceof NshBuiltinDefault) {
                    return false;
                }
            }
            Predicate<NshBuiltin> filter = configuration.getBuiltinFilter();
            if (filter != null) {
                if (!filter.test(nshBuiltin)) {
                    return false;
                }
            }
            return true;
        }
    }
}
