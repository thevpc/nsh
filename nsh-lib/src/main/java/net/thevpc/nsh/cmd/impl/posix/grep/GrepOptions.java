package net.thevpc.nsh.cmd.impl.posix.grep;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nsh.cmd.impl.util.NNumberedObject;
import net.thevpc.nsh.cmd.impl.util.filter.JavaExceptionWindowFilter;
import net.thevpc.nsh.cmd.impl.util.filter.WindowFilterBuilder;
import net.thevpc.nsh.util.FileInfo;

import java.util.ArrayList;
import java.util.List;

class GrepOptions implements NCmdLineConfigurable {

    public boolean byLine;
    public boolean summary;
    //        boolean regexp = false;
    boolean requireNutsOptions = false;
    boolean withNutsOptions = false;
    boolean invertMatch = false;
    boolean recursive = false;
    boolean followSymbolicLinks = true;
    List<String> fileNames = new ArrayList<>();
    List<String> fileNamesIgnoreCase = new ArrayList<>();
    boolean word = false;
    boolean lineRegexp = false;
    boolean ignoreCase = false;
    String highlighter;
    String selectionStyle;
    WindowFilterBuilder<NNumberedObject<String>> windowFilter = new WindowFilterBuilder<>();
    boolean n = false;
    int windowBefore = 0;
    int windowAfter = 0;
    Long from;
    Long to;
    List<FileInfo> files = new ArrayList<>();
    List<ExpressionInfo> expressions = new ArrayList<>();
    NSession session;
    JavaExceptionWindowFilter lastJavaExceptionWindowFilter=null;

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        return false;
    }

    @Override
    public GrepOptions configure(boolean skipUnsupported, String... args) {
        configure(skipUnsupported, NCmdLine.of(args).setCommandName("grep"));
        return this;
    }

}
