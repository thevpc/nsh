package net.thevpc.nsh.options;

import net.thevpc.nuts.cmdline.NCmdLine;

public interface NshOptionsParser {
    NshOptions parse(NCmdLine args);
}
