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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import net.thevpc.nsh.cmd.NshBuiltinDefault;
import net.thevpc.nsh.eval.NshExecutionContext;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;

/**
 * wc - print newline, word, and byte counts for each file
 * 
 * @author thevpc
 */
@NComponentScope(NScopeType.WORKSPACE)
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class WcCommand extends NshBuiltinDefault {

    public WcCommand() {
        super("wc", Options.class);
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        NArg a;
        
        if ((a = cmdLine.nextFlag("-l", "--lines").orNull()) != null) {
            options.lines = a.getBooleanValue().get();
            return true;
        } else if ((a = cmdLine.nextFlag("-w", "--words").orNull()) != null) {
            options.words = a.getBooleanValue().get();
            return true;
        } else if ((a = cmdLine.nextFlag("-c", "--bytes").orNull()) != null) {
            options.bytes = a.getBooleanValue().get();
            return true;
        } else if ((a = cmdLine.nextFlag("-m", "--chars").orNull()) != null) {
            options.chars = a.getBooleanValue().get();
            return true;
        } else if ((a = cmdLine.nextFlag("-L", "--max-line-length").orNull()) != null) {
            options.maxLineLength = a.getBooleanValue().get();
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
        NPrintStream out = context.getSession().out();

        // If no specific options are set, show all by default (lines, words, bytes)
        if (!options.lines && !options.words && !options.bytes && !options.chars && !options.maxLineLength) {
            options.lines = true;
            options.words = true;
            options.bytes = true;
        }

        long totalLines = 0;
        long totalWords = 0;
        long totalBytes = 0;
        long totalChars = 0;
        long totalMaxLength = 0;

        try {
            if (options.files.isEmpty()) {
                // Read from stdin
                FileStats stats = countFile(context.in(), session);
                printStats(stats, null, options, out);
            } else {
                // Process each file
                for (String filePath : options.files) {
                    NPath path = NPath.of(filePath);
                    if (!path.exists()) {
                        context.err().print(NMsg.ofC("wc: %s: No such file or directory\n", filePath));
                        continue;
                    }
                    
                    try (InputStream in = path.getInputStream()) {
                        FileStats stats = countFile(in, session);
                        printStats(stats, filePath, options, out);
                        
                        totalLines += stats.lines;
                        totalWords += stats.words;
                        totalBytes += stats.bytes;
                        totalChars += stats.chars;
                        if (stats.maxLineLength > totalMaxLength) {
                            totalMaxLength = stats.maxLineLength;
                        }
                    } catch (IOException e) {
                        context.err().print(NMsg.ofC("wc: %s: %s\n", filePath, e.getMessage()));
                    }
                }

                // Print total if multiple files
                if (options.files.size() > 1) {
                    FileStats totalStats = new FileStats();
                    totalStats.lines = totalLines;
                    totalStats.words = totalWords;
                    totalStats.bytes = totalBytes;
                    totalStats.chars = totalChars;
                    totalStats.maxLineLength = totalMaxLength;
                    printStats(totalStats, "total", options, out);
                }
            }
        } catch (Exception e) {
            throw new net.thevpc.nuts.command.NExecutionException(NMsg.ofC("wc: error: %s", e.getMessage()), e, 1);
        }
    }

    private FileStats countFile(InputStream inputStream, NSession session) throws IOException {
        FileStats stats = new FileStats();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stats.lines++;
                
                // Count bytes: line bytes + newline (1 byte for \n)
                // Note: getBytes() is necessary for accurate UTF-8 byte counting
                stats.bytes += line.getBytes().length + 1; // +1 for newline
                stats.chars += line.length() + 1; // +1 for newline
                
                // Count words manually (more efficient than regex split)
                stats.words += countWords(line);
                
                // Track max line length
                if (line.length() > stats.maxLineLength) {
                    stats.maxLineLength = line.length();
                }
            }
        }
        
        return stats;
    }
    
    /**
     * Count words in a line manually (more efficient than regex split)
     * A word is a sequence of non-whitespace characters
     */
    private int countWords(String line) {
        if (line == null || line.isEmpty()) {
            return 0;
        }
        
        int wordCount = 0;
        boolean inWord = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            boolean isWhitespace = Character.isWhitespace(c);
            
            if (isWhitespace) {
                inWord = false;
            } else {
                if (!inWord) {
                    wordCount++;
                    inWord = true;
                }
            }
        }
        
        return wordCount;
    }

    private void printStats(FileStats stats, String filename, Options options, NPrintStream out) {
        StringBuilder sb = new StringBuilder();
        
        if (options.lines) {
            sb.append(String.format("%7d ", stats.lines));
        }
        if (options.words) {
            sb.append(String.format("%7d ", stats.words));
        }
        if (options.bytes) {
            sb.append(String.format("%7d ", stats.bytes));
        }
        if (options.chars) {
            sb.append(String.format("%7d ", stats.chars));
        }
        if (options.maxLineLength) {
            sb.append(String.format("%7d ", stats.maxLineLength));
        }
        
        if (filename != null) {
            sb.append(filename);
        }
        
        out.println(sb.toString());
    }

    private static class FileStats {
        long lines = 0;
        long words = 0;
        long bytes = 0;
        long chars = 0;
        long maxLineLength = 0;
    }

    private static class Options {
        boolean lines = false;
        boolean words = false;
        boolean bytes = false;
        boolean chars = false;
        boolean maxLineLength = false;
        List<String> files = new ArrayList<>();
    }
}
