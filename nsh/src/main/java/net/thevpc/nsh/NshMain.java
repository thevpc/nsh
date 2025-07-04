package net.thevpc.nsh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineRunner;

import net.thevpc.nsh.options.DefaultNshOptionsParser;
import net.thevpc.nsh.options.NshOptions;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;

import java.util.logging.Level;

import net.thevpc.nuts.util.NMsg;

@NAppDefinition
public class NshMain  {

    public static void main(String[] args) {
        NApp.builder(args).run();
    }

    @NAppInstaller
    public void onInstallApplication() {
        NLogOp log = NLogOp.of(NshMain.class);
        log.level(Level.CONFIG).verb(NLogVerb.START).log(NMsg.ofPlain("[nsh] Installation..."));
        NApp.of().runCmdLine(new NCmdLineRunner() {
            @Override
            public void init(NCmdLine cmdLine) {
                cmdLine.setCommandName("nsh --nuts-exec-mode=install");
            }

            @Override
            public void run(NCmdLine cmdLine) {
                Nsh c = new Nsh(new NshConfig()
                        .setIncludeDefaultBuiltins(true).setIncludeExternalExecutor(true)
                );
                c.installToNuts();
            }
        });
    }

    @NAppUpdater
    public void onUpdateApplication() {
        NLogOp log = NLogOp.of(NshMain.class);
        log.level(Level.CONFIG).verb(NLogVerb.INFO).log(NMsg.ofPlain("[nsh] update..."));
        onInstallApplication();
    }

    @NAppUninstaller
    public void onUninstallApplication() {
        NLogOp log = NLogOp.of(NshMain.class);
        Nsh.uninstallFromNuts();
    }

    @NAppRunner
    public void run() {

        //before loading Nsh check if we need to activate rich term
        DefaultNshOptionsParser options = new DefaultNshOptionsParser();
        NshOptions o = options.parse(NApp.of().getCmdLine().toStringArray());

//        if (o.isEffectiveInteractive()) {
//            session.getWorkspace().io().term().enableRichTerm(session);
//        }
        new Nsh(new NshConfig()
                .setIncludeDefaultBuiltins(true).setIncludeExternalExecutor(true)
        ).run();
    }

}
