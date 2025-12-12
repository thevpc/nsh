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
 * mv - move (rename) files
 * 
 * @author thevpc
 */
@NComponentScope(NScopeType.WORKSPACE)
public class MvCommand extends NshBuiltinDefault {

    public MvCommand() {
        super("mv", DEFAULT_SCORE, Options.class);
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        NArg a;
        
        if ((a = cmdLine.nextFlag("-f", "--force").orNull()) != null) {
            options.force = a.getBooleanValue().get();
            return true;
        } else if ((a = cmdLine.nextFlag("-i", "--interactive").orNull()) != null) {
            options.interactive = a.getBooleanValue().get();
            return true;
        } else if ((a = cmdLine.nextFlag("-n", "--no-clobber").orNull()) != null) {
            options.noClobber = a.getBooleanValue().get();
            return true;
        } else if ((a = cmdLine.nextFlag("-v", "--verbose").orNull()) != null) {
            options.verbose = a.getBooleanValue().get();
            return true;
        }
        return false;
    }

    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        String path = cmdLine.next().get().image();
        options.paths.add(path);
        return true;
    }

    @Override
    protected void main(NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();

        if (options.paths.size() < 2) {
            context.err().println(NMsg.ofC("mv: missing file operand"));
            throw new net.thevpc.nuts.command.NExecutionException(NMsg.ofPlain("mv: missing file operand"), 1);
        }

        // Last argument is the destination
        String destPath = options.paths.get(options.paths.size() - 1);
        NPath dest = NPath.of(destPath);
        
        // All other arguments are sources
        List<String> sources = options.paths.subList(0, options.paths.size() - 1);
        
        // Cache isDirectory() result to avoid redundant filesystem calls
        // When multiple sources, destination must be a directory (checked below)
        // For single source, we check inside the loop
        Boolean destIsDirectory = null;
        if (sources.size() > 1) {
            if (!dest.isDirectory()) {
                context.err().println(NMsg.ofC("mv: target '%s' is not a directory", destPath));
                throw new net.thevpc.nuts.command.NExecutionException(
                    NMsg.ofPlain("mv: target is not a directory"), 1);
            }
            destIsDirectory = true; // Cache the result since we know it's a directory
        }
        
        for (String sourcePath : sources) {
            try {
                NPath source = NPath.of(sourcePath);
                
                if (!source.exists()) {
                    context.err().println(NMsg.ofC("mv: cannot stat '%s': No such file or directory", sourcePath));
                    continue;
                }
                
                NPath targetPath;
                // Use cached result if available, otherwise check
                boolean isDestDir = (destIsDirectory != null) ? destIsDirectory : dest.isDirectory();
                if (isDestDir) {
                    // Moving into a directory - keep the filename
                    String filename = source.getName();
                    targetPath = dest.resolve(filename);
                } else {
                    // Renaming or moving to specific path
                    targetPath = dest;
                }
                
                // Check if target exists
                if (targetPath.exists()) {
                    if (options.noClobber) {
                        if (options.verbose) {
                            context.out().println(NMsg.ofC("mv: not overwriting '%s'", targetPath));
                        }
                        continue;
                    }
                    
                    if (options.interactive && !options.force) {
                        // In non-interactive mode, we'll just skip confirmation
                        // A real implementation would prompt the user
                        if (options.verbose) {
                            context.out().println(NMsg.ofC("mv: overwriting '%s'", targetPath));
                        }
                    }
                }
                
                // Perform the move
                source.moveTo(targetPath);
                
                if (options.verbose) {
                    context.out().println(NMsg.ofC("'%s' -> '%s'", sourcePath, targetPath));
                }
                
            } catch (Exception e) {
                context.err().println(NMsg.ofC("mv: cannot move '%s': %s", sourcePath, e.getMessage()));
            }
        }
    }

    private static class Options {
        boolean force = false;
        boolean interactive = false;
        boolean noClobber = false;
        boolean verbose = false;
        List<String> paths = new ArrayList<>();
    }
}
