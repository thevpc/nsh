package net.thevpc.nsh.eval;


import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineAutoComplete;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nsh.cmd.alias.NshAliasManager;
import net.thevpc.nsh.cmd.NshBuiltin;
import net.thevpc.nsh.cmd.NshBuiltinManager;
import net.thevpc.nsh.sys.NshFileSystem;
import net.thevpc.nsh.Nsh;
import net.thevpc.nsh.parser.nodes.NshFunctionManager;
import net.thevpc.nsh.parser.nodes.NshNode;
import net.thevpc.nsh.parser.nodes.NshVariables;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;

public class DefaultNshExecutionContext implements NshExecutionContext {


    private NshContext shellContext;
    private NSession session;
    private NshBuiltin builtin;
    private NTerminalMode terminalMode = null;
    private boolean askHelp;
    private boolean askVersion;
    private Object options;

    public DefaultNshExecutionContext(NshContext shellContext, NshBuiltin command) {
        this.shellContext = shellContext;
        //each execution has its very own session!
        this.session = shellContext.getSession().copy();
        this.builtin = command;
    }


    @Override
    public NWorkspace getWorkspace() {
        return shellContext.getWorkspace();
    }

    @Override
    public NSession getSession() {
        return session;
    }


    @Override
    public Nsh getShell() {
        return shellContext.nsh();
    }

    @Override
    public NPrintStream out() {
        return getSession().out();
    }

    @Override
    public NPrintStream err() {
        return getSession().err();
    }

    @Override
    public InputStream in() {
        return getSession().in();
    }


    @Override
    public NshContext getShellContext() {
        return shellContext;
    }
    @Override
    public boolean configureFirst(NCmdLine cmd) {
        NArg a = cmd.peek().get();
        if (a == null) {
            return false;
        }
        switch(a.key()) {
            case "--help": {
                cmd.skip();
                setAskHelp(true);
                while(cmd.hasNext()){
                    getSession().configureLast(cmd);
                }
                break;
            }
            case "--version": {
                cmd.skip();
                setAskVersion(true);
                while(cmd.hasNext()){
                    getSession().configureLast(cmd);
                }
                break;
//                cmd.skip();
//                if (cmd.isExecMode()) {
//                    out().print(NMsg.ofC("%s%n", NutsIdResolver.of(getSession()).resolveId(getClass()).getVersion().toString()));
//                    cmd.skipAll();
//                }
//                throw new NutsExecutionException(shellContext.getSession(), NMsg.ofC("Help"), 0);
            }
            default: {
                if (getSession() != null && getSession().configureFirst(cmd)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isAskVersion() {
        return askVersion;
    }

    public DefaultNshExecutionContext setAskVersion(boolean askVersion) {
        this.askVersion = askVersion;
        return this;
    }

    @Override
    public void configureLast(NCmdLine cmd) {
        if (!configureFirst(cmd)) {
            cmd.throwUnexpectedArgument();
        }
    }

    @Override
    public NTerminalMode geTerminalMode() {
        return terminalMode;
    }

    @Override
    public boolean isAskHelp() {
        return askHelp;
    }

    @Override
    public DefaultNshExecutionContext setAskHelp(boolean askHelp) {
        this.askHelp = askHelp;
        return this;
    }

    public <T> T getOptions() {
        return (T) options;
    }

    public DefaultNshExecutionContext setOptions(Object options) {
        this.options = options;
        return this;
    }

    @Override
    public NshNode getRootNode() {
        return shellContext.getRootNode();
    }

    @Override
    public NshNode getParentNode() {
        return shellContext.getParentNode();
    }

    @Override
    public NshVariables vars() {
        return shellContext.vars();
    }

    @Override
    public NshFunctionManager functions() {
        return shellContext.functions();
    }

    @Override
    public NshExecutionContext setOut(PrintStream out) {
        getSession().getTerminal().setErr(NPrintStream.of(out));
        return this;
    }

    @Override
    public NshExecutionContext setErr(PrintStream err) {
        getSession().getTerminal().setErr(NPrintStream.of(err));
        return this;
    }

    @Override
    public NshExecutionContext setIn(InputStream in) {
        getSession().getTerminal().setIn(in);
        return this;
    }

    @Override
    public NshExecutionContext setEnv(Map<String, String> env) {
        shellContext.setEnv(env);
        return this;
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return shellContext.getUserProperties();
    }

    @Override
    public String getDirectory() {
        return shellContext.getDirectory();
    }

    @Override
    public String getHome() {
        return shellContext.getHome();
    }

    @Override
    public void setDirectory(String cwd) {
        shellContext.setDirectory(cwd);
    }

    @Override
    public NshFileSystem getFileSystem() {
        return shellContext.getFileSystem();
    }

    @Override
    public String getAbsolutePath(String path) {
        return shellContext.getAbsolutePath(path);
    }

    @Override
    public String[] expandPaths(String path) {
        return shellContext.expandPaths(path);
    }

    @Override
    public NshContext getParentContext() {
        return shellContext.getParentContext();
    }

    @Override
    public NshAliasManager aliases() {
        return shellContext.aliases();
    }

    @Override
    public NshBuiltinManager builtins() {
        return shellContext.builtins();
    }

    @Override
    public String getServiceName() {
        return shellContext.getServiceName();
    }

//    public void setArgs(String[] args) {
//        shellContext.setArgs(args);
//    }
//
//    public String getArg(int index) {
//        return shellContext.getArg(index);
//    }
//
//    public int getArgsCount() {
//        return shellContext.getArgsCount();
//    }
//
//    public String[] getArgsArray() {
//        return shellContext.getArgsArray();
//    }
//
//    public List<String> getArgsList() {
//        return shellContext.getArgsList();
//    }

    @Override
    public NshExecutionContext setSession(NSession session) {
        this.session=session;
        return this;
    }

    @Override
    public NCmdLineAutoComplete getAutoComplete() {
        return shellContext.getAutoComplete();
    }
}
