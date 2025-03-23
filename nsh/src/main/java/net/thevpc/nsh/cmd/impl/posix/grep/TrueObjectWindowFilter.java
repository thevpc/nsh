package net.thevpc.nsh.cmd.impl.posix.grep;

import net.thevpc.nsh.cmd.impl.util.WindowFilter;

public class TrueObjectWindowFilter<T> implements WindowFilter<T> {
    @Override
    public boolean accept(T line) {
        return true;
    }

    @Override
    public WindowFilter<T> copy() {
        return this;
    }
}
