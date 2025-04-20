package net.thevpc.nsh.history;

import net.thevpc.nuts.io.NPath;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Collections;
import java.util.List;

public class NoHistory implements NshHistory {
    @Override
    public NPath getHistoryFile() {
        return null;
    }

    @Override
    public NshHistory setHistoryFile(NPath historyFile) {
        return this;
    }

    @Override
    public void add(String e) {

    }

    @Override
    public void removeDuplicates() {

    }

    @Override
    public List<String> getElements() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getElements(int maxElements) {
        return Collections.emptyList();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void clear() {

    }

    @Override
    public void remove(int index) {

    }

    @Override
    public void load() {

    }

    @Override
    public void load(NPath reader) {

    }

    @Override
    public void load(Reader reader) {

    }

    @Override
    public void save() {

    }

    @Override
    public void save(NPath writer) {

    }

    @Override
    public void save(PrintWriter writer) {

    }

    @Override
    public void save(PrintStream writer) {

    }

    @Override
    public void append(NshHistory other) {

    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public String get(int index) {
        return null;
    }

    @Override
    public String getLast() {
        return "";
    }
}
