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
import net.thevpc.nsh.util.MemResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
}
