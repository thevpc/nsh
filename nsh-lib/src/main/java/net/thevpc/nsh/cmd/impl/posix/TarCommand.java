package net.thevpc.nsh.cmd.impl.posix;

import net.thevpc.nsh.cmd.NshBuiltinDefault;
import net.thevpc.nsh.eval.NshExecutionContext;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NArgName;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NExecutionException;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NScore;

import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class TarCommand extends NshBuiltinDefault {

    public TarCommand() {
        super("tar", Options.class);
    }

    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        String path = cmdLine.nextNonOption(NArgName.of("file")).flatMap(NArg::asString).get();
        NPath file = NPath.of(path).toAbsolute(context.getDirectory());
        options.files.add(file);
        return true;
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        NArg a;

        // Main operation modes (mutually exclusive)
        if ((a = cmdLine.nextFlag("-c").orNull()) != null) {
            options.create = a.getBooleanValue().get();
            return true;
        } else if ((a = cmdLine.nextFlag("-x").orNull()) != null) {
            options.extract = a.getBooleanValue().get();
            return true;
        } else if ((a = cmdLine.nextFlag("-t").orNull()) != null) {
            options.list = a.getBooleanValue().get();
            return true;
        }
        // Modifiers
        else if ((a = cmdLine.nextFlag("-z").orNull()) != null) {
            options.gzip = a.getBooleanValue().get();
            return true;
        } else if ((a = cmdLine.nextFlag("--skip-root").orNull()) != null) {
            options.skipRoot = a.getBooleanValue().get();
            return true;
        }
        // Arguments
        else if ((a = cmdLine.nextEntry("-f").orNull()) != null) {
            options.archiveFile = a.getStringValue().get();
            return true;
        } else if ((a = cmdLine.nextEntry("-C").orNull()) != null) {
            options.targetDir = a.getStringValue().get();
            return true;
        } else if ((a = cmdLine.nextFlag("-J").orNull()) != null) {
            options.xz = a.getBooleanValue().get();
            return true;
        }
        // Non-options (files to archive, or files to extract)
        else if (cmdLine.isNextOption()) {
            return false; // Let framework handle unknown options
        }

        return false;
    }

    @Override
    protected void main(NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();

        // 1. Validate mutually exclusive main modes
        int modes = (options.create ? 1 : 0) + (options.extract ? 1 : 0) + (options.list ? 1 : 0);
        if (modes == 0) {
            cmdLine.throwError(NMsg.ofPlain("tar: you must specify one of the main operation modes: -c (create), -x (extract), or -t (list)"));
        }
        if (modes > 1) {
            cmdLine.throwError(NMsg.ofPlain("tar: you may only specify one of the main operation modes: -c, -x, or -t"));
        }

        // 2. Validate archive file
        if (NBlankable.isBlank(options.archiveFile)) {
            cmdLine.throwError(NMsg.ofPlain("tar: you must specify the archive file with -f <archive>"));
        }

        NPath archivePath = NPath.of(options.archiveFile).toAbsolute(context.getDirectory());
        String packaging;
        if (options.xz) {
            packaging = "txz";   // or "txz" – your NUncompressTarXz should recognise both
        } else if (options.gzip) {
            packaging = "tgz";
        } else {
            packaging = "tar";
        }

        try {
            if (options.create) {
                if (options.files.isEmpty()) {
                    cmdLine.throwError(NMsg.ofPlain("tar: missing input files to archive"));
                }

                NCompress compress = NCompress.of()
                        .packaging(packaging)
                        .target(archivePath)
                        .skipRoot(options.skipRoot);

                for (NPath file : options.files) {
                    compress.addSource(file);
                }
                compress.run();

            } else if (options.extract || options.list) {
                NUncompress uncompress = NUncompress.of()
                        .from(archivePath)
                        .packaging(packaging)
                        .skipRoot(options.skipRoot);

                if (options.list) {
                    uncompress.visit(new NUncompressVisitor() {
                        @Override
                        public boolean visitFolder(String path) {
                            NOut.println(NMsg.ofStyledPath(path + "/"));
                            return true;
                        }

                        @Override
                        public boolean visitFile(String path, InputStream inputStream) {
                            NOut.println(NMsg.ofStyledPath(path));
                            return true;
                        }
                    }).run();
                } else {
                    String dir = options.targetDir;
                    if (NBlankable.isBlank(dir)) {
                        dir = context.getDirectory();
                    } else {
                        dir = context.getAbsolutePath(dir);
                    }

                    uncompress.to(NPath.of(dir)).run();
                }
            }
        } catch (UncheckedIOException | NIOException ex) {
            throw new NExecutionException(NMsg.ofC("tar operation failed: %s", ex), ex, NExecutionException.ERROR_1);
        }
    }

    private static class Options {
        boolean create = false;
        boolean extract = false;
        boolean list = false;
        boolean gzip = false;
        boolean xz = false;          // ← new flag for XZ compression
        boolean skipRoot = false;
        String archiveFile = null;
        String targetDir = null;
        List<NPath> files = new ArrayList<>();
    }

}