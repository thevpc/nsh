package net.thevpc.nsh.cmd.impl.posix.grep;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyles;

public class NoGrepFilter implements GrepFilter {
    public NoGrepFilter() {
    }

    public void processNonPivot(String line, NTextBuilder coloredLine, NTextStyles selectionStyle, NSession session) {

    }
        @Override
    public boolean processPivot(String line, NTextBuilder coloredLine, NTextStyles selectionStyle, NSession session) {
        return true;
    }
}
