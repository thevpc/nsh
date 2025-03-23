package net.thevpc.nsh.parser.nodes;

import net.thevpc.nsh.eval.NshContext;

import java.util.*;

/**
 * Created by vpc on 11/4/16.
 */
public class NshVariables {

    private Map<String, NshVar> vars = new HashMap<>();
    private List<NshVarListener> listeners = new ArrayList<>();
    private NshContext shellContext;

    public NshVariables(NshContext shellContext) {
        this.shellContext=shellContext;
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

    public void setParent(NshVariables parent) {
        if (parent != null) {
            for (NshVar value : parent.vars.values()) {
                this.vars.put(value.getName(), new NshVar(this, value.getName(), value.getValue(), value.isExported()));
            }
        }
    }

    public NshVar getVar(String name) {
        NshVar v = findVar(name);
        if (v == null) {
            throw new NoSuchElementException("not found " + name);
        }
        return v;
    }

    public NshVar findVar(String name) {
        NshVar t = vars.get(name);
        if (t != null) {
            return t;
        }
        return null;
    }

    public String get(String name) {
        return get(name, null);
    }

    public String get(String name, String defaultValue) {
        NshVar v = findVar(name);
        if (v != null) {
            return v.getValue();
        }
        return defaultValue;
    }

    public Properties getExported() {
        Properties all = new Properties();
        for (NshVar value : vars.values()) {
            if (value.isExported()) {
                all.put(value.getName(), value.getValue());
            }
        }
        return all;
    }

    public Properties getAll() {
        Properties all = new Properties();
        for (NshVar value : vars.values()) {
            all.put(value.getName(), value.getValue());
        }
        return all;
    }

    public void set(Map<String, String> env) {
        for (Map.Entry<String, String> entry : env.entrySet()) {
            String key = (String) entry.getKey();
            set(key, (String) entry.getValue());
            if (vars.containsKey(key)) {
                export(key);
            }
        }
    }

    public void export(String var, String value) {
        NshVar b = findVar(var);
        if (value == null) {
            value = var;
        }
        if (b == null) {
            vars.put(var, new NshVar(this, var, value, true));
        } else {
            b.setValue(value);
            b.setExported(true);
        }
    }

    public void set(String var, String value) {
        set(var, value, false);
    }

    public void set(String var, String value, boolean defaultExport) {
        NshVar b = findVar(var);
        if (b == null && value == null) {
            return;
        }
        if (b == null) {
            NshVar jvar = new NshVar(this, var, value, defaultExport);
            vars.put(var, jvar);
            for (NshVarListener listener : getVarListeners()) {
                listener.varAdded(jvar,this,shellContext);
            }
            for (NshVarListener listener : shellContext.nsh().getVarListeners()) {
                listener.varAdded(jvar,this,shellContext);
            }
        } else {
            String oldValue = b.getValue();
            if(!Objects.equals(oldValue,value)) {
                b.setValue(value);
                for (NshVarListener listener : getVarListeners()) {
                    listener.varValueUpdated(b,oldValue,this,shellContext);
                }
                for (NshVarListener listener : shellContext.nsh().getVarListeners()) {
                    listener.varValueUpdated(b,oldValue,this,shellContext);
                }
            }
        }
    }

    public void export(String var) {
        NshVar b = findVar(var);
        if (b == null) {
            set(var, var, true);
        } else {
            if (!b.isExported()) {
                b.setExported(true);
                for (NshVarListener listener : getVarListeners()) {
                    listener.varExportUpdated(b,false,this,shellContext);
                }
                for (NshVarListener listener : shellContext.nsh().getVarListeners()) {
                    listener.varExportUpdated(b,false,this,shellContext);
                }
            }
        }
    }

    public void unexport(String var) {
        if (vars.containsKey(var)) {
            NshVar jvar = getVar(var);
            if(jvar.isExported()) {
                jvar.setExported(false);
            }
            for (NshVarListener listener : getVarListeners()) {
                listener.varExportUpdated(jvar,false,this,shellContext);
            }
            for (NshVarListener listener : shellContext.nsh().getVarListeners()) {
                listener.varExportUpdated(jvar,false,this,shellContext);
            }
        } else {
            throw new NoSuchElementException("Unable to unexport env var " + var + " . Not found");
        }
    }

    public boolean isExported(String var) {
        NshVar v = findVar(var);
        return v != null && v.isExported();
    }

    public void set(NshVariables other) {
        for (Map.Entry<Object, Object> entry : other.getAll().entrySet()) {
            String key = (String) entry.getKey();
            set(key, (String) entry.getValue());
            if (other.isExported(key)) {
                export(key);
            }
        }
    }

    public void clear() {

    }

    void varValueChanged(NshVar svar, String oldValue) {
        if (svar.getValue() == null) {
            vars.remove(svar.getName());
        }
    }

    void varEnabledChanged(NshVar aThis) {

    }

}
