package net.thevpc.nsh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineRunner;

import net.thevpc.nsh.options.DefaultNshOptionsParser;
import net.thevpc.nsh.options.NshOptions;

import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;

import java.util.logging.Level;

import net.thevpc.nuts.util.NMsg;

@NAppDefinition
public class NshMain  {

    public static void main(String[] args) {
        NApp.builder(args).run();
    }

    @NAppInstaller
    public void onInstallApplication() {
        NLog log = NLog.of(NshMain.class);
        log.log(NMsg.ofPlain("[nsh] Installation...")
                .withLevel(Level.CONFIG).withIntent(NMsgIntent.START)
        );
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
        NLog log = NLog.of(NshMain.class);
        log.log(NMsg.ofPlain("[nsh] update...")
                .withLevel(Level.CONFIG).withIntent(NMsgIntent.INFO)
        );
        onInstallApplication();
    }

    @NAppUninstaller
    public void onUninstallApplication() {
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
