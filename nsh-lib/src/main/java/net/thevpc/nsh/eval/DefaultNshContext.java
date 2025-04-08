/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nsh.eval;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLineAutoComplete;
import net.thevpc.nsh.cmd.alias.DefaultNshAliasManager;
import net.thevpc.nsh.cmd.alias.NshAliasManager;
import net.thevpc.nsh.cmd.DefaultNshCommandManager;
import net.thevpc.nsh.cmd.NshBuiltinManager;
import net.thevpc.nsh.cmd.NBuiltinManager;
import net.thevpc.nsh.sys.DefaultNshFileSystem;
import net.thevpc.nsh.sys.NshFileSystem;
import net.thevpc.nsh.Nsh;
import net.thevpc.nsh.parser.nodes.DefaultNshFunctionManager;
import net.thevpc.nsh.parser.nodes.NshFunctionManager;
import net.thevpc.nsh.parser.nodes.NshNode;
import net.thevpc.nsh.parser.nodes.NshVariables;

import java.util.*;

/**
 * @author thevpc
 */
public class DefaultNshContext extends AbstractNshContext {

    private static final NshResult OK_RESULT = new NshResult(0, null, null);
    public String oldCmdLine = null;
    public NshResult lastResult = OK_RESULT;
    public NshContext parentContext;
    public int commandLineIndex = -1;
    private Nsh shell;
    private NshVariables vars;
    private NshNode rootNode;
    private NshNode parentNode;
    private Map<String, Object> userProperties = new HashMap<>();
    private NshFunctionManager functionManager = new DefaultNshFunctionManager();
    private NshAliasManager aliasManager = new DefaultNshAliasManager();
    private NshBuiltinManager builtinManager;
    private String cwd = System.getProperty("user.dir");
    private NshFileSystem fileSystem;
    private NCmdLineAutoComplete autoComplete;

    public DefaultNshContext(Nsh shell, NshNode rootNode, NshNode parentNode,
                             NshContext parentContext, NWorkspace workspace, NSession session, NshVariables vars,
                             String serviceName, String[] args
    ) {
        this(parentContext);
        workspace = (workspace != null ? workspace : parentContext != null ? parentContext.getWorkspace() : null);
        if (session == null) {
            if (workspace != null) {
                session = getWorkspace().createSession();
            }
        }
        setSession(session);
        setServiceName(serviceName);
        setArgs(args);
        this.vars = new NshVariables(this);
        this.shell = shell;
        setFileSystem(new DefaultNshFileSystem());
        if (parentContext != null) {
            setDirectory(parentContext.getDirectory());
        }
        setRootNode(rootNode);
        setParentNode(parentNode);
        if (parentContext != null) {
            vars().set(parentContext.vars());
            setBuiltins(parentContext.builtins());
            for (String a : parentContext.aliases().getAll()) {
                aliases().set(a, parentContext.aliases().get(a));
            }
        } else {
            for (Map.Entry<String, String> entry : NWorkspace.of().getSysEnv().entrySet()) {
                vars().export(entry.getKey(), entry.getValue());
            }
            setBuiltins(new NBuiltinManager());
            NshAliasManager a = aliases();
            a.set(".", "source");
            a.set("[", "test");

            a.set("ll", "ls");
            a.set("..", "cd ..");
            a.set("...", "cd ../..");
        }
        if (vars != null) {
            for (Map.Entry<Object, Object> entry : vars.getAll().entrySet()) {
                vars().set((String) entry.getKey(), (String) entry.getValue());
            }
        }

        this.parentContext = parentContext;//.copy();
        if (parentContext != null) {
            setDirectory(parentContext.getDirectory());
        }
    }

    //    public DefaultNshContext(Nsh shell, NshFunctionManager functionManager, NshAliasManager aliasManager,NshVariables env, NshNode root, NshNode parent, InputStream in, PrintStream out, PrintStream err, String... args) {
//        setShell(shell);
//        setVars(env);
//        setAliases(aliasManager);
//        setFunctionManager(functionManager);
//        setRoot(root);
//        setParent(parent);
//        setIn(in);
//        setOut(out);
//        setErr(err);
//        setArgs(args);
//    }
    public DefaultNshContext(NshContext other) {
        this.parentContext = other;
        copyFrom(other);
    }

    @Override
    public Nsh nsh() {
        return shell;
    }

    @Override
    public NshNode getRootNode() {
        return rootNode;
    }

    public NshContext setRootNode(NshNode root) {
        this.rootNode = root;
        return this;
    }

    @Override
    public NshNode getParentNode() {
        return parentNode;
    }

    @Override
    public NshContext setParentNode(NshNode parent) {
        this.parentNode = parent;
        return this;
    }


//    public NshContext copy() {
//        DefaultNshContext c = new DefaultNshContext(shell);
//        c.copyFrom(this);
//        return c;
//    }

    @Override
    public NshVariables vars() {
        return vars;
    }


    @Override
    public NshFunctionManager functions() {
        return functionManager;
    }


    @Override
    public NshContext setEnv(Map<String, String> env) {
        if (env != null) {
            this.vars.set(env);
        }
        return this;
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return userProperties;
    }

    @Override
    public String getDirectory() {
        return cwd;
    }

    @Override
    public String getHome() {
        return System.getProperty("user.home");
    }

    @Override
    public void setDirectory(String cwd) {
        NshFileSystem fs = getFileSystem();
        if (cwd == null || cwd.isEmpty()) {
            this.cwd = fs.getHomeWorkingDir(getSession());
        } else {
            String r =
                    fs.isAbsolute(cwd, getSession()) ? cwd :
                            fs.getAbsolutePath(this.cwd + "/" + cwd, getSession());
            if (fs.exists(r, getSession())) {
                if (fs.isDirectory(r, getSession())) {
                    this.cwd = r;
                } else {
                    throw new IllegalArgumentException("not a directory : " + cwd);
                }
            } else {
                throw new IllegalArgumentException("no such file or directory : " + cwd);
            }
        }
    }

    @Override
    public NshFileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public void setFileSystem(NshFileSystem fileSystem) {
        this.fileSystem = fileSystem;
        setDirectory(this.fileSystem.getInitialWorkingDir(getSession()));
    }

    @Override
    public NshContext getParentContext() {
        return parentContext;
    }

    @Override
    public NshAliasManager aliases() {
        return aliasManager;
    }

    @Override
    public void setBuiltins(NshBuiltinManager builtinsManager) {
        this.builtinManager = builtinsManager;
    }

    @Override
    public NshBuiltinManager builtins() {
        if (builtinManager == null) {
            builtinManager = new DefaultNshCommandManager();
        }
        return builtinManager;
    }

    @Override
    public NshResult getLastResult() {
        return lastResult;
    }

    @Override
    public void setLastResult(NshResult lastResult) {
        this.lastResult = lastResult == null ? OK_RESULT : lastResult;
    }

    public void setAliases(NshAliasManager aliasManager) {
        this.aliasManager = aliasManager == null ? new DefaultNshAliasManager() : aliasManager;
    }

    public void copyFrom(NshContext other) {
        if (other != null) {
            super.copyFrom(other);
            this.shell = other.nsh();
            this.vars = other.vars();
            this.userProperties = new HashMap<>();
            this.userProperties.putAll(other.getUserProperties());
            this.parentContext = other.getParentContext();
        }
    }


    @Override
    public NCmdLineAutoComplete getAutoComplete() {
        return autoComplete;
    }

    @Override
    public void setAutoComplete(NCmdLineAutoComplete autoComplete) {
        this.autoComplete = autoComplete;
    }

    public void setFunctionManager(NshFunctionManager functionManager) {
        this.functionManager = functionManager == null ? new DefaultNshFunctionManager() : functionManager;
    }


}
