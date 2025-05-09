package net.thevpc.nsh.history;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NBlankable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class DefaultNshHistory extends AbstractNshHistory {
    private List<String> history = new ArrayList<>();
    private NPath historyFile;

    @Override
    public NPath getHistoryFile() {
        return historyFile;
    }

    @Override
    public DefaultNshHistory setHistoryFile(NPath historyFile) {
        this.historyFile = historyFile;
        return this;
    }

    public void add(String e) {
        if (!NBlankable.isBlank(e)) {
            String l = getLast();
            if (!e.equals(l)) {
                history.add(e);
            }
        }
    }

    @Override
    public void removeDuplicates() {
        LinkedHashSet<String> vals = new LinkedHashSet<>();
        for (String s : history) {
            vals.add(s);
        }
        history.clear();
        history.addAll(vals);
    }

    @Override
    public List<String> getElements() {
        return new ArrayList<>(history);
    }

    @Override
    public List<String> getElements(int maxElements) {
        if (maxElements < 0) {
            return new ArrayList<>(history);
        }
        List<String> all = new ArrayList<>();
        if (maxElements <= 0 || maxElements > history.size()) {
            maxElements = history.size();
        }
        for (int i = 0; i < maxElements; i++) {
            all.add(history.get(history.size() - maxElements + i));
        }
        return all;
    }


    public int size() {
        return history.size();
    }

    public void clear() {
        history.clear();
    }

    public void remove(int index) {
        if (index >= 0 && index < history.size()) {
            history.remove(index);
        }
    }

    @Override
    public void load() {
        load(getHistoryFile());
    }

    @Override
    public void save() {
        save(getHistoryFile());
    }

    @Override
    public boolean isEmpty() {
        return history.isEmpty();
    }

    @Override
    public String get(int index) {
        if (index < 0 || index >= history.size()) {
            return null;
        }
        return history.get(index);
    }

    @Override
    public String getLast() {
        return get(size() - 1);
    }
}
