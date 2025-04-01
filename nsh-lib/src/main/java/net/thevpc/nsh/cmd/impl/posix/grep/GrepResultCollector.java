package net.thevpc.nsh.cmd.impl.posix.grep;

import net.thevpc.nsh.cmd.impl.util.NNumberedObject;
import net.thevpc.nsh.util.FileInfo;

public interface GrepResultCollector {
    boolean acceptMatch(GrepResultItem item);
    long getLinesCount();
    long getMatchCount();
    long getFilesCount();

    void acceptFile(FileInfo f);

    void acceptLine(NNumberedObject<String> line);
}
