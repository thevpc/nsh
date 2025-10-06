package net.thevpc.nsh.eval;

import net.thevpc.nuts.cmdline.NCmdLineAutoComplete;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nsh.cmd.alias.NshAliasManager;
import net.thevpc.nsh.options.autocomplete.NshAutoCompleteCandidate;
import net.thevpc.nsh.cmd.NshBuiltin;
import net.thevpc.nsh.cmd.NshBuiltinManager;
import net.thevpc.nsh.sys.NshFileSystem;
import net.thevpc.nsh.Nsh;
import net.thevpc.nsh.parser.nodes.NshFunctionManager;
import net.thevpc.nsh.parser.nodes.NshNode;
import net.thevpc.nsh.parser.nodes.NshVariables;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

/**
 * Created by vpc on 11/4/16.
 */
public interface NshContext {

    Nsh nsh();

    NshNode getRootNode();

    NshContext setRootNode(NshNode root);

    NshNode getParentNode();

    NshContext setParentNode(NshNode parent);

    InputStream in();

    NPrintStream out();

    NPrintStream err();

    NshVariables vars();

    Watcher bindStreams(InputStream out, InputStream err, OutputStream in);


    NshFunctionManager functions();


    NshContext setOut(PrintStream out);

    NshContext setErr(PrintStream out);

    NshContext setIn(InputStream in);

    NshExecutionContext createCommandContext(NshBuiltin command);

    List<NshAutoCompleteCandidate> resolveAutoCompleteCandidates(String commandName, List<String> autoCompleteWords, int wordIndex, String autoCompleteLine);

    NshContext setEnv(Map<String, String> env);

    Map<String, Object> getUserProperties();

    String getDirectory();

    String getHome();

    void setDirectory(String cwd);

    NshFileSystem getFileSystem();

    void setFileSystem(NshFileSystem fileSystem);

    String getAbsolutePath(String path);

    String[] expandPaths(String path);

    NshContext getParentContext();

    NshAliasManager aliases();

    void setBuiltins(NshBuiltinManager commandManager);

    NshBuiltinManager builtins();

    NshResult getLastResult();

    void setLastResult(NshResult result);

    void setAliases(NshAliasManager aliasManager);

    void copyFrom(NshContext other);

    String getServiceName();

    void setServiceName(String serviceName);

    void setArgs(String[] args);

    String getArg(int index);

    int getArgsCount();

    String[] getArgsArray();

    List<String> getArgsList();

//    NshContext copy() ;

    NSession getSession();

    NshContext setSession(NSession session);

    NWorkspace getWorkspace();

    NCmdLineAutoComplete getAutoComplete();

    void setAutoComplete(NCmdLineAutoComplete value);

    void setFunctionManager(NshFunctionManager functionManager);

    interface Watcher {
        void stop();

        boolean isStopped();
    }

}
