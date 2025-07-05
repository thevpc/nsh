package net.thevpc.nuts.tutorial.customshell;

import net.thevpc.nuts.NApplication;
import net.thevpc.nuts.NSession;
import net.thevpc.nsh.NShell;
import net.thevpc.nsh.NShellConfiguration;

/**
 * @author vpc
 */
@NApp.Definition
public class CustomShell {

    public static void main(String[] args) {
        NApp.builder(args).run();
    }

    @NApp.Main
    public void run() {
        NShell sh = new NShell(new NShellConfiguration());
        sh.run();
    }

}
