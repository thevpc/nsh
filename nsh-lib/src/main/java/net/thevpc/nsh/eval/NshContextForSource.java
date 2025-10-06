package net.thevpc.nsh.eval;

import net.thevpc.nuts.cmdline.NCmdLineAutoComplete;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nsh.cmd.alias.NshAliasManager;
import net.thevpc.nsh.cmd.NshBuiltinManager;
import net.thevpc.nsh.sys.NshFileSystem;
import net.thevpc.nsh.Nsh;
import net.thevpc.nsh.parser.nodes.NshFunctionManager;
import net.thevpc.nsh.parser.nodes.NshNode;
import net.thevpc.nsh.parser.nodes.NshVariables;

import java.util.Map;

public class NshContextForSource extends AbstractNshContext {
    private NshContext other;

    public NshContextForSource(NshContext other) {
        this.other = other;
    }

    @Override
    public Nsh nsh() {
        return other.nsh();
    }

    @Override
    public NshNode getRootNode() {
        return other.getRootNode();
    }

    @Override
    public NshContext setRootNode(NshNode root) {
        other.setRootNode(root);
        return this;
    }

    @Override
    public NSession getSession() {
        NSession s = super.getSession();
        if(s!=null){
            return s;
        }
        return other.getSession();
    }

    @Override
    public NWorkspace getWorkspace() {
        return getSession().getWorkspace();
    }

    @Override
    public NshNode getParentNode() {
        return other.getParentNode();
    }

    @Override
    public NshContext setParentNode(NshNode parent) {
        other.setParentNode(parent);
        return this;
    }

    @Override
    public NshVariables vars() {
        return other.vars();
    }

    @Override
    public NshFunctionManager functions() {
        return other.functions();
    }

    @Override
    public NshContext setEnv(Map<String, String> env) {
        other.setEnv(env);
        return this;
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return other.getUserProperties();
    }

    @Override
    public String getDirectory() {
        return other.getDirectory();
    }

    @Override
    public String getHome() {
        return other.getHome();
    }

    @Override
    public void setDirectory(String cwd) {
        other.setDirectory(cwd);
    }

    @Override
    public NshFileSystem getFileSystem() {
        return other.getFileSystem();
    }

    @Override
    public void setFileSystem(NshFileSystem fileSystem) {
        other.setFileSystem(fileSystem);
    }

    @Override
    public NshContext getParentContext() {
        return other.getParentContext();
    }

    @Override
    public NshAliasManager aliases() {
        return other.aliases();
    }

    @Override
    public void setBuiltins(NshBuiltinManager commandManager) {
        other.setBuiltins(commandManager);
    }

    @Override
    public NshBuiltinManager builtins() {
        return other.builtins();
    }

    @Override
    public NshResult getLastResult() {
        return other.getLastResult();
    }

    @Override
    public void setLastResult(NshResult result) {
        other.setLastResult(result);
    }

    @Override
    public void setAliases(NshAliasManager aliasManager) {
        other.setAliases(aliasManager);
    }

    @Override
    public NCmdLineAutoComplete getAutoComplete() {
        return other.getAutoComplete();
    }

    @Override
    public void setAutoComplete(NCmdLineAutoComplete value) {
        other.setAutoComplete(value);
    }

    @Override
    public void setFunctionManager(NshFunctionManager functionManager) {
        other.setFunctionManager(functionManager);
    }
}
