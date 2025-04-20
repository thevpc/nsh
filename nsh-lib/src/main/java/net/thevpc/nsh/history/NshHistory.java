package net.thevpc.nsh.history;

import net.thevpc.nuts.io.NPath;

import java.io.*;
import java.util.List;

public interface NshHistory {
    NPath getHistoryFile();

    NshHistory setHistoryFile(NPath historyFile);

    void add(String e);

    void removeDuplicates();

    List<String> getElements();

    List<String> getElements(int maxElements);

    //    void save();
//
//    void load();
//
    int size();

    void clear();

    void remove(int index);

    void load();

    void load(NPath reader);

    void load(Reader reader);

    void save();

    void save(NPath writer);

    void save(PrintWriter writer);

    void save(PrintStream writer);

    void append(NshHistory other);

    boolean isEmpty();

    String get(int index);

    String getLast();
}
