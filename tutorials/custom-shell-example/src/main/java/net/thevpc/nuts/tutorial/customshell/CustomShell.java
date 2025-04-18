package net.thevpc.nuts.tutorial.customshell;

import net.thevpc.nuts.NApplication;
import net.thevpc.nuts.NSession;
import net.thevpc.nsh.NShell;
import net.thevpc.nsh.NShellConfiguration;

/**
 * @author vpc
 */
public class CustomShell implements NApplication {

    public static void main(String[] args) {
        new CustomShell().main(NMainArgs.ofExit(args));
    }

    @Override
    public void run() {
        NShell sh = new NShell(new NShellConfiguration());
        sh.run();
    }

}
