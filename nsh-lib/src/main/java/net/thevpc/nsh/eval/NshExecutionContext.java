package net.thevpc.nsh.eval;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineAutoComplete;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nsh.cmd.alias.NshAliasManager;
import net.thevpc.nsh.cmd.NshBuiltinManager;
import net.thevpc.nsh.sys.NshFileSystem;
import net.thevpc.nsh.Nsh;
import net.thevpc.nsh.parser.nodes.NshFunctionManager;
import net.thevpc.nsh.parser.nodes.NshNode;
import net.thevpc.nsh.parser.nodes.NshVariables;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;

public interface NshExecutionContext {

    Nsh getShell();

    InputStream in();

    NPrintStream out();

    NPrintStream err();

    NWorkspace getWorkspace();

    NSession getSession();

    boolean configureFirst(NCmdLine cmd);

    boolean isAskVersion();

    void configureLast(NCmdLine cmd);

    NshContext getShellContext();

    NTerminalMode geTerminalMode();

    boolean isAskHelp();

    DefaultNshExecutionContext setAskHelp(boolean askHelp);

    <T> T getOptions();

    NshExecutionContext setOptions(Object options);

    NshNode getRootNode();

    NshNode getParentNode();

    NshVariables vars();

    NshFunctionManager functions();

    NshExecutionContext setOut(PrintStream out);

    NshExecutionContext setErr(PrintStream err);

    NshExecutionContext setIn(InputStream in);

    NshExecutionContext setEnv(Map<String, String> env);

    Map<String, Object> getUserProperties();

    String getDirectory();

    String getHome();

    void setDirectory(String cwd);

    NshFileSystem getFileSystem();

    String getAbsolutePath(String path);

    String[] expandPaths(String path);

    NshContext getParentContext();

    NshAliasManager aliases();

    NshBuiltinManager builtins();

    String getServiceName();

    NshExecutionContext setSession(NSession session);

    NCmdLineAutoComplete getAutoComplete();
}
