package net.thevpc.nsh.options;

import net.thevpc.nuts.cmdline.NCmdLine;

public class DefaultNshOptionsParser implements NshOptionsParser {

    public DefaultNshOptionsParser() {
    }

    protected NshOptions createOptions() {
        return new NshOptions();
    }

    @Override
    public NshOptions parse(NCmdLine args) {
        NshOptions options = createOptions();
        createMatcher(args, options).requireAllDefaults();
        postParse(options);
        return options;
    }

    protected void postParse(NshOptions options) {
        if (options.isInteractive() ||
                (options.getFiles().isEmpty()
                        && !options.isReadCommandsFromStdIn()
                        && !options.isCommand())
        ) {
            options.setEffectiveInteractive(true);
        }
    }

    protected NCmdLine.Matcher createMatcher(NCmdLine args, NshOptions options) {
        NCmdLine.Matcher m = args.matcher();
        m.with("-?", "--help").matchTrueFlag(a -> options.setHelp(true));
        m.with("--version").matchTrueFlag(a -> options.setVersion(true));
        m.with("-v", "--verbose").matchFlag(a -> options.setVerbose(a.booleanValue()));
        m.with("-x").matchFlag(a -> options.setXtrace(a.booleanValue()));
        m.with("-i").matchFlag(a -> options.setInteractive(a.booleanValue()));
        m.with("-s").matchFlag(a -> options.setReadCommandsFromStdIn(a.booleanValue()));
        m.with("-r", "--restricted").matchFlag(a -> options.setRestricted(a.booleanValue()));
        m.with("-l").matchFlag(a -> options.setLogin(a.booleanValue()));
        m.with("-D", "--dump-strings").matchFlag(a -> options.setDumpStrings(a.booleanValue()));
        m.with("--dump-po-strings").matchFlag(a -> options.setDumpPoStrings(a.booleanValue()));
        m.with("--noediting").matchFlag(a -> options.setNoEditing(a.booleanValue()));
        m.with("--noprofile").matchFlag(a -> options.setNoProfile(a.booleanValue()));
        m.with("--norc").matchFlag(a -> options.setNoRc(a.booleanValue()));
        m.with("--posix").matchFlag(a -> options.setPosix(a.booleanValue()));
        m.with("--bash").matchFlag(a -> options.setBash(a.booleanValue()));
        m.with("-c").matchAnyMultiple(a -> {
            options.setBash(a.nextFlag().get().booleanValue());
            options.setCommand(true);
            if (!a.isEmpty()) {
                options.setServiceName(a.next().get().image());
            }
            options.setCommandArgs(a.toStringList());
            a.skipAll();
        });
        m.with("--rcfile", "--init-file").matchEntry(a -> options.setRcFile(a.stringValue()));
        m.with("--startup-script").matchEntry(a -> options.setStartupScript(a.stringValue()));
        m.with("--shutdown-script").matchEntry(a -> options.setShutdownScript(a.stringValue()));

        m.with("--").matchAnyMultiple(a -> {
            a.next().get();
            if (options.isReadCommandsFromStdIn()) {
                options.getCommandArgs().addAll(a.toStringList());
            } else {
                options.getFiles().addAll(a.toStringList());
            }
            a.skipAll();
        });

        m.with("--").matchAnyMultiple(a -> {
            a.next().get();
            options.setLogin(true);
            if (options.isReadCommandsFromStdIn()) {
                options.getCommandArgs().addAll(a.toStringList());
            } else {
                options.getFiles().addAll(a.toStringList());
            }
            a.skipAll();
        });
        m.withNonOption().matchAnyMultiple(a -> {
            options.getFiles().add(a.next().get().image());
            options.getCommandArgs().addAll(a.toStringList());
            a.skipAll();
            options.setExitAfterProcessingLines(true);
        });
        m.withDefaults();
        return m;
    }

}
