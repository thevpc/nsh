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
package net.thevpc.nsh.cmd.impl.posix;

import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nsh.cmd.NshBuiltinDefault;
import net.thevpc.nsh.eval.NshExecutionContext;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class DateCommand extends NshBuiltinDefault {

    public DateCommand() {
        super("date", DEFAULT_SCORE,Options.class);
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        NArg a = cmdLine.peek().get();
        switch(a.key()) {
            case "-d":
            case "--date": {
                if (context.getShell().getOptions().isNsh()) {
                    a = cmdLine.nextEntry().get();
                    if (a.isUncommented()) {
                        options.date = a.getStringValue().get();
                    }
                } else {
                    a = cmdLine.next().get();
                    options.date = a.getStringValue().get();
                }
                return true;
            }
            case "-f":
            case "--file": {
                if (context.getShell().getOptions().isNsh()) {
                    a = cmdLine.nextEntry().get();
                    if (a.isUncommented()) {
                        options.file = a.getStringValue().get();
                    }
                } else {
                    a = cmdLine.next().get();
                    options.file = a.getStringValue().get();
                }
                return true;
            }
            case "--rfc-3339": {
                if (context.getShell().getOptions().isNsh()) {
                    a = cmdLine.next().get();
                    if (a.isUncommented()) {
                        String s = a.getStringValue().get();
                        if (s == null) {
                            s = "";
                        }
                        options.rfc3339 = s;
                    }
                } else {
                    a = cmdLine.next().get();
                    String s = a.getStringValue().get();
                    if (s == null) {
                        s = "";
                    }
                    options.rfc3339 = s;
                }
                return true;
            }
            case "--iso-8601": {
                if (context.getShell().getOptions().isNsh()) {
                    a = cmdLine.next().get();
                    if (a.isUncommented()) {
                        String s = a.getStringValue().get();
                        if (s == null) {
                            s = "";
                        }
                        options.rfc8601 = s;
                    }
                } else {
                    a = cmdLine.next().get();
                    String s = a.getStringValue().get();
                    if (s == null) {
                        s = "";
                    }
                    options.rfc8601 = s;
                }
                return true;
            }
            case "-s":
            case "--set": {
                if (context.getShell().getOptions().isNsh()) {
                    a = cmdLine.next().get();
                    if (a.isUncommented()) {
                        String s = a.getStringValue().get();
                        if (s == null) {
                            s = "";
                        }
                        options.setdate = s;
                    }
                } else {
                    a = cmdLine.next().get();
                    String s = a.getStringValue().get();
                    if (s == null) {
                        s = "";
                    }
                    options.setdate = s;
                }
                return true;
            }
            case "-Id":
            case "-Idate":
            case "-Ih":
            case "-Ihours":
            case "-Im":
            case "-Iminutes":
            case "-Is":
            case "-Iseconds":
            case "-Ins": {
                if (context.getShell().getOptions().isNsh()) {
                    a = cmdLine.next().get();
                    if (a.isUncommented()) {
                        options.rfc8601 = a.asString().get().substring(2);
                    }
                } else {
                    a = cmdLine.next().get();
                    options.rfc8601 = a.asString().get().substring(2);
                }
                return true;
            }
            case "--debug": {
                if (context.getShell().getOptions().isNsh()) {
                    a = cmdLine.nextFlag().get();
                    if (a.isUncommented()) {
                        options.debug = a.getBooleanValue().get();
                    }
                } else {
                    a = cmdLine.next().get();
                    options.debug = true;
                }
                return true;
            }
            case "-u":
            case "--utc":
            case "--universal": {
                if (context.getShell().getOptions().isNsh()) {
                    a = cmdLine.nextFlag().get();
                    if (a.isUncommented()) {
                        options.utc = a.getBooleanValue().get();
                    }
                } else {
                    a = cmdLine.next().get();
                    options.utc = true;
                }
                return true;
            }
            case "-R":
            case "--rfc-email": {
                if (context.getShell().getOptions().isNsh()) {
                    a = cmdLine.nextFlag().get();
                    if (a.isUncommented()) {
                        options.rfcMail = a.getBooleanValue().get();
                    }
                } else {
                    a = cmdLine.next().get();
                    options.rfcMail = true;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    protected void main(NCmdLine cmdLine, NshExecutionContext context) {
        Options options = context.getOptions();
        ZonedDateTime dateTimeInMyZone = ZonedDateTime.
                of(LocalDateTime.now(), ZoneId.systemDefault());
        if (options.utc) {
            dateTimeInMyZone = dateTimeInMyZone.withZoneSameInstant(ZoneOffset.UTC);
        }
        if (options.iso != null) {
            switch (options.iso) {
                case "":
                case "d":
                case "date": {
                    NOut.println(dateTimeInMyZone.toLocalDate());
                    break;
                }
                case "m":
                case "minutes": {
                    NOut.println(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm").format(dateTimeInMyZone.toLocalDateTime()));
                    break;
                }
                case "s":
                case "seconds": {
                    NOut.println(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss").format(dateTimeInMyZone.toLocalDateTime()));
                    break;
                }
                case "ns": {
                    NOut.println(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSSSSSSS").format(dateTimeInMyZone.toLocalDateTime()));
                    break;
                }
                default: {
                    //error??
                    NOut.println(dateTimeInMyZone.toLocalDate());
                    break;
                }
            }
        } else if (options.rfc3339 != null) {
            switch (options.rfc3339) {
                case "":
                case "d":
                case "date": {
                    NOut.println(dateTimeInMyZone.toLocalDate());
                    break;
                }
                case "m":
                case "minutes": {
                    NOut.println(DateTimeFormatter.ofPattern("uuuu-MM-dd' 'HH:mmXXX").format(dateTimeInMyZone.toLocalDate()));
                    break;
                }
                case "s":
                case "seconds": {
                    NOut.println(DateTimeFormatter.ofPattern("uuuu-MM-dd' 'HH:mm:ssXXX").format(dateTimeInMyZone.toLocalDateTime()));
                    break;
                }
                case "ns": {
                    NOut.println(DateTimeFormatter.ofPattern("uuuu-MM-dd' 'HH:mm:ss.SSSSSSSSSXXX").format(dateTimeInMyZone.toLocalDateTime()));
                    break;
                }
                default: {
                    //error??
                    NOut.println(dateTimeInMyZone.toLocalDate());
                    break;
                }
            }
        } else if (options.rfcMail) {
            NOut.println(DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss Z").format(dateTimeInMyZone));
        } else {
            NOut.println(DateTimeFormatter.ofPattern("EEE MMM d hh:mm:ss a Z yyyy").format(dateTimeInMyZone));
        }
    }

    @Override
    protected void init(NCmdLine cmdLine, NshExecutionContext context) {
        for (String s : new String[]{
                "-Id", "-Idate",
                "-Ih", "-Ihours",
                "-Im", "-Iminutes",
                "-Is", "-Iseconds",
                "-Ins"
        }) {
            cmdLine.registerSpecialSimpleOption(s);
        }
    }

    private static class Options {
        //other date
        String date;
        boolean debug;
        String file;
        String iso;
        boolean rfcMail;
        String rfc8601;
        String rfc3339;
        String reference;
        String setdate;
        boolean utc;
    }

    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        return nextOption(arg, cmdLine, context);
    }
}
