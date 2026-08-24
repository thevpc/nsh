package net.thevpc.nsh;

import net.thevpc.nuts.app.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineRunner;

import net.thevpc.nsh.options.DefaultNshOptionsParser;
import net.thevpc.nsh.options.NshOptions;

import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;

import java.util.logging.Level;

import net.thevpc.nuts.text.NMsg;

@NApp
public class NshMain {

    public static void main(String[] args) {
        NApplication.builder(args).run();
    }

    @NAppInstall
    public void onInstallApplication() {
        NLog log = NLog.of(NshMain.class);
        log.log(NMsg.ofPlain("[nsh] Installation...")
                .withLevel(Level.CONFIG).withIntent(NMsgIntent.START)
        );
        NApplication.of().runCmdLine(new NCmdLineRunner() {
            @Override
            public void init(NCmdLine cmdLine) {
                cmdLine.commandName("nsh --nuts-exec-mode=install");
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

    @NAppUpdate
    public void onUpdateApplication() {
        NLog log = NLog.of(NshMain.class);
        log.log(NMsg.ofPlain("[nsh] update...")
                .withLevel(Level.CONFIG).withIntent(NMsgIntent.NOTICE));
        onInstallApplication();
    }

    @NAppUninstall
    public void onUninstallApplication() {
        Nsh.uninstallFromNuts();
    }

    @NAppRun
    public void run() {

        //before loading Nsh check if we need to activate rich term
        DefaultNshOptionsParser options = new DefaultNshOptionsParser();
        NCmdLine cmdLine = NApplication.of().cmdLine();
        NshOptions o = options.parse(cmdLine);
        new Nsh(new NshConfig()
                .setIncludeDefaultBuiltins(true).setIncludeExternalExecutor(true)
        ).run();
    }

    @NAppComplete
    public void complete() {
        DefaultNshOptionsParser options = new DefaultNshOptionsParser();
        NCmdLine cmdLine = NApplication.of().cmdLine();
        options.parse(cmdLine);
        cmdLine.printCompleteResult();
    }

}
