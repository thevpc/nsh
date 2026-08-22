package net.thevpc.nsh.options;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineMatcher;

public class DefaultNshOptionsParser implements NshOptionsParser {

    public DefaultNshOptionsParser() {
    }

    protected NshOptions createOptions() {
        return new NshOptions();
    }

    @Override
    public NshOptions parse(NCmdLine args) {
        NshOptions options = createOptions();
        createMatcher(args, options)
                .requireAll();
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

    protected NCmdLineMatcher createMatcher(NCmdLine args, NshOptions options) {
        NCmdLineMatcher m = args.matcher();
        m.when("-?", "--help").asTrueFlag(a -> options.setHelp(true));
        m.when("--version").asTrueFlag(a -> options.setVersion(true));
        m.when("-v", "--verbose").asFlag(a -> options.setVerbose(a.booleanValue()));
        m.when("-x").asFlag(a -> options.setXtrace(a.booleanValue()));
        m.when("-i").asFlag(a -> options.setInteractive(a.booleanValue()));
        m.when("-s").asFlag(a -> options.setReadCommandsFromStdIn(a.booleanValue()));
        m.when("-r", "--restricted").asFlag(a -> options.setRestricted(a.booleanValue()));
        m.when("-l").asFlag(a -> options.setLogin(a.booleanValue()));
        m.when("-D", "--dump-strings").asFlag(a -> options.setDumpStrings(a.booleanValue()));
        m.when("--dump-po-strings").asFlag(a -> options.setDumpPoStrings(a.booleanValue()));
        m.when("--noediting").asFlag(a -> options.setNoEditing(a.booleanValue()));
        m.when("--noprofile").asFlag(a -> options.setNoProfile(a.booleanValue()));
        m.when("--norc").asFlag(a -> options.setNoRc(a.booleanValue()));
        m.when("--posix").asFlag(a -> options.setPosix(a.booleanValue()));
        m.when("--bash").asFlag(a -> options.setBash(a.booleanValue()));
        m.when("-c").asRaw(a -> {
            options.setBash(a.nextFlag().get().booleanValue());
            options.setCommand(true);
            if (!a.isEmpty()) {
                options.setServiceName(a.next().get().image());
            }
            options.setCommandArgs(a.toStringList());
            a.skipAll();
        });
        m.when("--rcfile", "--init-file").asEntry(a -> options.setRcFile(a.stringValue()));
        m.when("--startup-script").asEntry(a -> options.setStartupScript(a.stringValue()));
        m.when("--shutdown-script").asEntry(a -> options.setShutdownScript(a.stringValue()));

        m.when("--").asRaw(a -> {
            a.next().get();
            if (options.isReadCommandsFromStdIn()) {
                options.getCommandArgs().addAll(a.toStringList());
            } else {
                options.getFiles().addAll(a.toStringList());
            }
            a.skipAll();
        });

        m.when("--").asRaw(a -> {
            a.next().get();
            options.setLogin(true);
            if (options.isReadCommandsFromStdIn()) {
                options.getCommandArgs().addAll(a.toStringList());
            } else {
                options.getFiles().addAll(a.toStringList());
            }
            a.skipAll();
        });
        m.whenNonOption().asRaw(a -> {
            options.getFiles().add(a.next().get().image());
            options.getCommandArgs().addAll(a.toStringList());
            a.skipAll();
            options.setExitAfterProcessingLines(true);
        });
        m.withDefaults();
        return m;
    }

}
