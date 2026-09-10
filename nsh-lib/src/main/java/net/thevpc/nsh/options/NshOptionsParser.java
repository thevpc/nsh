package net.thevpc.nsh.options;

import net.thevpc.nuts.cmdline.NCmdLine;

public interface NshOptionsParser {
    NshOptions parse(NCmdLine args);

    default NshOptions parse(NCmdLine args, NshOptions options) {
        return parse(args);
    }
}
