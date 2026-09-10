/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nsh.test;

import net.thevpc.nsh.Nsh;
import net.thevpc.nsh.NshConfig;
import net.thevpc.nsh.options.NshOptions;
import net.thevpc.nsh.util.MemResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 *
 * @author thevpc
 */
public class TestCommands {

    @BeforeAll
    static void openWorkspace(){
        TestUtils.openNewTestWorkspace("--verbose");
    }

    @Test
    public void testDirname() {
        Nsh c = new Nsh(new NshConfig()
                .setIncludeDefaultBuiltins(true).setIncludeExternalExecutor(true)
                .setArgs()
                );
        MemResult r = c.executeCommand(new String[]{"dirname", "/", "a", "/a", "/a/"});
        Assertions.assertEquals(
                "/\n"
                        + ".\n"
                        + "/\n"
                        + "/", r.out().trim());
    }


    @Test
    public void testBasename() {
        Nsh c = new Nsh(new NshConfig()
                .setIncludeDefaultBuiltins(true).setIncludeExternalExecutor(true)
                .setArgs()
        );
        MemResult r = c.executeCommand(new String[]{"basename", "-a", "/", "a", "/a", "/a/"});
        Assertions.assertEquals(
                "/\n"
                        + "a\n"
                        + "a\n"
                        + "a", r.out().trim());
    }

    @Test
    public void testEnv() {
        Nsh c = new Nsh(new NshConfig()
                .setIncludeDefaultBuiltins(true).setIncludeExternalExecutor(true)
                .setArgs()
        );
        {
            MemResult r = c.executeCommand(new String[]{"env"});
            Assertions.assertTrue(r.out().contains("PWD="));
        }
        {
            MemResult r = c.executeCommand(new String[]{"env", "--json"});
            Assertions.assertTrue(r.out().contains("\"PWD\""));
        }
    }

    @Test
    public void testCheck() {
        Nsh c = new Nsh(
                new NshConfig()
                        .setIncludeDefaultBuiltins(true).setIncludeExternalExecutor(true)
                        .setArgs()
        );
        {
            MemResult r = c.executeCommand(new String[]{"test", "1", "-lt", "2"});
            Assertions.assertEquals(0, r.exitCode());
        }
        {
            MemResult r = c.executeCommand(new String[]{"test", "2", "-lt", "1"});
            Assertions.assertEquals(1, r.exitCode());
        }
    }

    @Test
    public void testCommandC() {
        Nsh c = new Nsh(new NshConfig()
                .setIncludeDefaultBuiltins(true).setIncludeExternalExecutor(true)
                .setArgs("-c", "echo hello world")
        );
        net.thevpc.nuts.core.NSession shellSession = c.getRootContext().getSession();
        shellSession.terminal(net.thevpc.nuts.io.NTerminal.ofMem());
        c.run();
        String result = shellSession.out().toString();
        Assertions.assertEquals("hello world\n", result);
    }

    @Test
    public void testCommandCUnquotedMultipleArgs() {
        Nsh c = new Nsh(new NshConfig()
                .setIncludeDefaultBuiltins(true).setIncludeExternalExecutor(true)
                .setArgs("-c", "echo", "hello", "world")
        );
        net.thevpc.nuts.core.NSession shellSession = c.getRootContext().getSession();
        shellSession.terminal(net.thevpc.nuts.io.NTerminal.ofMem());
        c.run();
        String result = shellSession.out().toString();
        Assertions.assertEquals("hello world\n", result);
    }

    @Test
    public void testCommandCPositionalArgs() {
        Nsh c = new Nsh(new NshConfig()
                .setIncludeDefaultBuiltins(true).setIncludeExternalExecutor(true)
                .setArgs("-c", "echo 0=$0 1=$1 2=$2", "myapp", "foo", "bar")
        );
        net.thevpc.nuts.core.NSession shellSession = c.getRootContext().getSession();
        shellSession.terminal(net.thevpc.nuts.io.NTerminal.ofMem());
        c.run();
        String result = shellSession.out().toString();
        Assertions.assertEquals("0=myapp 1=foo 2=bar\n", result);
    }

    @Test
    public void testCommandCBashPositionalArgs() {
        Nsh c = new Nsh(new NshConfig()
                .setIncludeDefaultBuiltins(true).setIncludeExternalExecutor(true)
                .setArgs("--bash", "-c", "echo 0=$0 1=$1", "myapp", "hello")
        );
        net.thevpc.nuts.core.NSession shellSession = c.getRootContext().getSession();
        shellSession.terminal(net.thevpc.nuts.io.NTerminal.ofMem());
        c.run();
        String result = shellSession.out().toString();
        Assertions.assertEquals("0=myapp 1=hello\n", result);
    }

    @Test
    public void testCommandCCompoundAndExitCode() {
        Nsh c = new Nsh(new NshConfig()
                .setIncludeDefaultBuiltins(true).setIncludeExternalExecutor(true)
                .setArgs("-c", "a=hello; echo $a; exit 42")
        );
        net.thevpc.nuts.core.NSession shellSession = c.getRootContext().getSession();
        shellSession.terminal(net.thevpc.nuts.io.NTerminal.ofMem());
        try {
            c.run();
            Assertions.fail("Expected NExecutionException with code 42");
        } catch (net.thevpc.nuts.command.NExecutionException ex) {
            Assertions.assertEquals(42, ex.exitCode());
        }
        String result = shellSession.out().toString();
        Assertions.assertEquals("hello\n", result);
    }

    @Test
    public void testProgrammaticNshOptions() {
        Nsh c = new Nsh(new NshConfig()
                .setIncludeDefaultBuiltins(true).setIncludeExternalExecutor(true)
                .setOptions(new NshOptions()
                        .setCommand(true)
                        .setCommandArgs(Arrays.asList("echo programmatic-options-test"))
                )
        );
        net.thevpc.nuts.core.NSession shellSession = c.getRootContext().getSession();
        shellSession.terminal(net.thevpc.nuts.io.NTerminal.ofMem());
        c.run();
        String result = shellSession.out().toString();
        Assertions.assertEquals("programmatic-options-test\n", result);
    }

    @Test
    public void testConfigAndOptionsAccessors() {
        NshConfig config = new NshConfig()
                .setAppName("custom-app")
                .setIncludeDefaultBuiltins(true)
                .setOptions(new NshOptions().setScriptName("custom-script"));
        Nsh c = new Nsh(config);

        Assertions.assertEquals("custom-app", c.getConfig().getAppName());
        Assertions.assertEquals("custom-app", c.getConfig().getServiceName());
        Assertions.assertEquals("custom-script", c.getOptions().getScriptName());
        Assertions.assertEquals("custom-script", c.getOptions().getServiceName());

        // Test configuration isolation
        c.getConfig().setAppName("mutated-app");
        Assertions.assertEquals("custom-app", c.getConfig().getAppName());
    }
}
