/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
package net.thevpc.nsh.cmd.impl.posix;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;

import net.thevpc.nsh.cmd.NshBuiltinDefault;
import net.thevpc.nsh.eval.NshExecutionContext;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.text.NMsg;

/**
 * touch - change file timestamps or create empty files
 * 
 * @author thevpc
 */
@NComponentScope(NScopeType.WORKSPACE)
public class TouchCommand extends NshBuiltinDefault {

    public TouchCommand() {
        super("touch", DEFAULT_SCORE, Options.class);
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        NArg a;
        
        if ((a = cmdLine.nextFlag("-a", "--time=atime", "--time=access", "--time=use").orNull()) != null) {
            options.onlyAccessTime = a.getBooleanValue().get();
            return true;
        } else if ((a = cmdLine.nextFlag("-m", "--time=mtime", "--time=modify").orNull()) != null) {
            options.onlyModTime = a.getBooleanValue().get();
            return true;
        } else if ((a = cmdLine.nextFlag("-c", "--no-create").orNull()) != null) {
            options.noCreate = a.getBooleanValue().get();
            return true;
        }
        return false;
    }

    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        String path = cmdLine.next().get().image();
        options.files.add(path);
        return true;
    }

    @Override
    protected void main(NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();

        if (options.files.isEmpty()) {
            context.err().println(NMsg.ofC("touch: missing file operand"));
            throw new net.thevpc.nuts.command.NExecutionException(NMsg.ofPlain("touch: missing file operand"), 1);
        }

        for (String filePath : options.files) {
            try {
                NPath path = NPath.of(filePath);
                
                if (!path.exists()) {
                    if (!options.noCreate) {
                        // Create the file
                        path.getParent().mkdirs();
                        path.writeString("");
                    }
                } else {
                    // Update timestamps efficiently using Files API
                    // This avoids reading/writing entire file content
                    try {
                        Path javaPath = Paths.get(path.toAbsolute().normalize().toString());
                        FileTime now = FileTime.fromMillis(System.currentTimeMillis());
                        
                        if (!options.onlyAccessTime) {
                            // Update modification time
                            Files.setLastModifiedTime(javaPath, now);
                        }
                        // Note: Access time update requires Files.setAttribute() which may not be
                        // supported on all file systems. For now, we rely on the read operation
                        // to update access time if needed.
                    } catch (Exception e) {
                        // Fallback to old method if Files API fails (e.g., for non-local paths)
                        if (!options.onlyAccessTime) {
                            byte[] content = path.readBytes();
                            path.writeBytes(content);
                        }
                    }
                }
            } catch (Exception e) {
                context.err().println(NMsg.ofC("touch: cannot touch '%s': %s", filePath, e.getMessage()));
            }
        }
    }

    private static class Options {
        boolean onlyAccessTime = false;
        boolean onlyModTime = false;
        boolean noCreate = false;
        List<String> files = new ArrayList<>();
    }
}
