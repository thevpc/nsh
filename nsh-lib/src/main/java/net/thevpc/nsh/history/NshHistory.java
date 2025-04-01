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

    void load() throws IOException;

    void load(NPath reader) throws IOException;

    void load(Reader reader) throws IOException;

    void save() throws IOException;

    void save(NPath writer) throws IOException;

    void save(PrintWriter writer) throws IOException;

    void save(PrintStream writer) throws IOException;

    void append(NshHistory other);

    boolean isEmpty();

    String get(int index);

    String getLast();
}
