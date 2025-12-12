/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nsh.eval;

import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nsh.err.NshException;
import net.thevpc.nsh.err.NshUniformException;
import net.thevpc.nsh.parser.nodes.NshCommandNode;
import net.thevpc.nsh.util.NshNonBlockingInputStream;
import net.thevpc.nsh.util.NshNonBlockingInputStreamAdapter;
import net.thevpc.nsh.util.ShellUtils;

import java.io.*;
import java.util.StringTokenizer;

/**
 * @author thevpc
 */
public class NshEvaluatorBase implements NshEvaluator {

    public static int readQuotes(char[] chars, int i, StringBuilder v) {
        return ShellUtils.readQuotes(chars, i, v);
    }

    protected int readAndEvalSimpleQuotesExpression(char[] chars, int i, StringBuilder out, NshContext context) {
        StringBuilder v = new StringBuilder();
        int count = readQuotes(chars, i, v);
        out.append(evalSimpleQuotesExpression(v.toString(), context));
        return i + count;
    }

    protected int readAndEvalAntiQuotesString(char[] chars, int i, StringBuilder out, NshContext context) {
        StringBuilder v = new StringBuilder();
        int count = readQuotes(chars, i, v);
        out.append(evalAntiQuotesExpression(v.toString(), context));
        return i + count;
    }

    protected int readAndEvalDblQuotesExpression(char[] chars, int i, StringBuilder out, NshContext context) {
        StringBuilder v = new StringBuilder();
        int count = readQuotes(chars, i, v);
        out.append(evalDoubleQuotesExpression(v.toString(), context));
        return i + count;
    }

    protected int readAndEvalDollarExpression(char[] chars, int i, StringBuilder out, boolean escapeResultPath, NshContext context) {
        if (i + 1 < chars.length) {
            i++;
            if (chars[i] == '{') {
                StringBuilder v = new StringBuilder();
                while (i < chars.length && chars[i] != '}') {
                    v.append(chars[i]);
                    i++;
                }
                String r = evalDollarExpression(v.toString(), context);
                if (escapeResultPath) {
                    r = context.nsh().escapePath(r);
                }
                out.append(r);
            } else {
                StringBuilder v = new StringBuilder();
                while (i < chars.length && chars[i] != ' ' && chars[i] != '\t') {
                    v.append(chars[i]);
                    i++;
                }
                String r = evalDollarExpression(String.valueOf(v.toString()), context);
                if (escapeResultPath) {
                    r = context.nsh().escapePath(r);
                }
                out.append(r);
            }
        }
        return i;
    }

    @Override
    public int evalSuffixOperation(String opString, NshCommandNode node, NshContext context) {
        switch (opString) {
            case "&": {
                return evalSuffixAndOperation(node, context);
            }
        }
        throw new NshException(NMsg.ofC("unsupported suffix operator %s", opString), 1);
    }

    @Override
    public int evalSuffixAndOperation(NshCommandNode node, NshContext context) {
        return context.nsh().evalNode(node, context);
    }

    @Override
    public int evalBinaryAndOperation(NshCommandNode left, NshCommandNode right, NshContext context) {
        int r = context.nsh().evalNode(left, context);
        if (r != 0) {
            return r;
        }
        return context.nsh().evalNode(right, context);
    }

    @Override
    public int evalBinaryOperation(String opString, NshCommandNode left, NshCommandNode right, NshContext context) {
        if (";".equals(opString)) {
            //no trace
        } else {
            context.nsh().traceExecution(() -> ("(" + left + ") " + opString + "(" + right + ")"), context);
        }
        if (";".equals(opString)) {
            return evalBinarySuiteOperation(left, right, context);
        } else if ("&&".equals(opString)) {
            return evalBinaryAndOperation(left, right, context);
        } else if ("||".equals(opString)) {
            return evalBinaryOrOperation(left, right, context);
        } else if ("|".equals(opString)) {
            return evalBinaryPipeOperation(left, right, context);
        } else {
            throw new NshException(NMsg.ofC("unsupported operator %s", opString),1);
        }
    }

    @Override
    public int evalBinaryOrOperation(final NshCommandNode left, NshCommandNode right, final NshContext context) {
        try {
            if (context.nsh().evalNode(left, context) == 0) {
                return 0;
            }
        } catch (NshUniformException e) {
            if (e.isQuit()) {
                e.throwQuit();
                return 0;
            }
        }
        return context.nsh().evalNode(right, context);
    }

    @Override
    public int evalBinaryPipeOperation(final NshCommandNode left, NshCommandNode right, final NshContext context) {
        final PipedOutputStream out;
        final PipedInputStream in;
        final NshNonBlockingInputStream in2;
        try {
            out = new PipedOutputStream();
            in = new PipedInputStream(out, 1024);
            in2 = (in instanceof NshNonBlockingInputStream) ? (NshNonBlockingInputStream) in : new NshNonBlockingInputStreamAdapter("jpipe-" + right.toString(), in);
        } catch (IOException ex) {
//            Logger.getLogger(BinoOp.class.getName()).log(Level.SEVERE, null, ex);
            throw new NshException(ex, 1);
        }
        final NshUniformException[] a = new NshUniformException[2];
        final PrintStream out1 = new PrintStream(out);
        final NshContext leftContext = context.nsh().createNewContext(context).setOut(out1);
        Thread j1 = new Thread() {
            @Override
            public void run() {
                try {
                    context.nsh().evalNode(left, leftContext);
                } catch (NshUniformException e) {
                    if (e.isQuit()) {
                        e.throwQuit();
                        return;
                    }
                    a[0] = e;
                }
                in2.noMoreBytes();
            }

        };
        j1.start();
        NshContext rightContext = context.nsh().createNewContext(context).setIn((InputStream) in2);
        try {
            context.nsh().evalNode(right, rightContext);
        } catch (NshUniformException e) {
            if (e.isQuit()) {
                e.throwQuit();
                return 0;
            }
            a[1] = e;
        }
        out1.flush();
        try {
            j1.join();
        } catch (Exception ex) {
            NLog.of(NshEvaluatorBase.class).log(NMsg.ofC("failed : %s", ex).asFinestFail(ex));
        }
        if (a[1] != null) {
            a[1].throwAny();
        }
        return 0;
    }

    @Override
    public int evalBinarySuiteOperation(NshCommandNode left, NshCommandNode right, NshContext context) {
        int r = 0;
        try {
            r = context.nsh().evalNode(left, context);
        } catch (NshUniformException e) {
            if (e.isQuit()) {
                e.throwQuit();
                return 0;
            }
        }
        if (r != 0 && context.nsh().getOptions().isErrExit()) {
            return r;
        }
        return context.nsh().evalNode(right, context);
    }

    @Override
    public String evalCommandAndReturnString(NshCommandNode command, NshContext context) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NshContext c2 = context.nsh().createNewContext(context, context.getServiceName(), context.getArgsArray());
        PrintStream p = new PrintStream(out);
        c2.setOut(p);
        context.nsh().evalNode(command, c2);
        p.flush();
        String cc = evalFieldSubstitutionAfterCommandSubstitution(out.toString(), context);
        return (context.nsh().escapeString(cc));
    }

    @Override
    public String evalDollarSharp(NshContext context) {
        return (String.valueOf(context.getArgsList().size()));
    }

    @Override
    public String evalDollarName(String name, NshContext context) {
        return (String.valueOf(context.vars().get(name, "")));
    }

    @Override
    public String evalDollarInterrogation(NshContext context) {
        return (String.valueOf(context.getLastResult().getCode()));
    }

    @Override
    public String evalDollarInteger(int index, NshContext context) {
        if (index < context.getArgsList().size()) {
            return (String.valueOf(context.getArg(index)));
        }
        return "";
    }

    @Override
    public String evalDollarExpression(String stringExpression, NshContext context) {
        String str = evalSimpleQuotesExpression(stringExpression, context);
        if (str.equals("#")) {
            return evalDollarSharp(context);
        } else if (str.equals("?")) {
            return evalDollarInterrogation(context);
        } else if (str.isEmpty()) {
            //do nothing
            return "";
        } else if (str.charAt(0) >= '0' && str.charAt(0) <= '9') {
            int index = Integer.parseInt(str, 10);
            return evalDollarInteger(index, context);
        } else {
            return evalDollarName(str, context);
        }
    }

    @Override
    public String evalSimpleQuotesExpression(String expressionString, NshContext context) {
//should replace wildcards ...
        StringBuilder sb = new StringBuilder();
        char[] chars = expressionString.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case '$': {
                    sb.append(chars[i]);
                    break;
                }
                case '\"': {
                    i = readAndEvalDblQuotesExpression(chars, i, sb, context);
                    break;
                }
                case '`': {
                    i = readAndEvalAntiQuotesString(chars, i, sb, context);
                    break;
                }
                default: {
                    sb.append(chars[i]);
                }
            }
        }
        return sb.toString();
    }

    @Override
    public String evalDoubleQuotesExpression(String stringExpression, NshContext context) {
//should replace wildcards ...
        StringBuilder sb = new StringBuilder();
        char[] chars = stringExpression.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case '$': {
                    i = readAndEvalDollarExpression(chars, i, sb, false, context);
                    break;
                }
                case '\'': {
                    i = readAndEvalSimpleQuotesExpression(chars, i, sb, context);
                    break;
                }
                case '`': {
                    i = readAndEvalAntiQuotesString(chars, i, sb, context);
                    break;
                }
                default: {
                    sb.append(chars[i]);
                }
            }
        }
        return sb.toString();
    }

    @Override
    public String evalAntiQuotesExpression(String stringExpression, NshContext context) {
        context.nsh().traceExecution(() -> ("`" + stringExpression + "`"), context);
        NshCommandNode t = context.nsh().parseScript(stringExpression);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NshContext c2 = context.nsh().createNewContext(context);
        c2.setOut(new PrintStream(out));
        context.nsh().evalNode(t, c2);
        c2.out().flush();
        return out.toString();
    }

    public String evalNoQuotesExpression(String stringExpression, NshContext context) {
        StringBuilder sb = new StringBuilder();
        char[] chars = stringExpression.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case '$': {
                    i = readAndEvalDollarExpression(chars, i, sb, true, context);
                    break;
                }
//                case '\"': {
//                    i=readAndEvalDblQuotesExpression(chars,i,sb,context);
//                    break;
//                }
//                case '\'': {
//                    i=readAndEvalSimpleQuotesExpression(chars,i,sb,context);
//                    break;
//                }
//                case '`': {
//                    i=readAndEvalAntiQuotesString(chars,i,sb,context);
//                    break;
//                }
                case '\\': {
                    i++;
                    sb.append(chars[i]);
                    break;
                }
                default: {
                    sb.append(chars[i]);
                }
            }
        }
        return sb.toString();
    }

    public String expandEnvVars(String stringExpression, boolean escapeResultPath, NshContext context) {
        StringBuilder sb = new StringBuilder();
        char[] chars = stringExpression.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case '$': {
                    i = readAndEvalDollarExpression(chars, i, sb, true, context);
                    break;
                }
                default: {
                    sb.append(chars[i]);
                }
            }
        }
        return sb.toString();
    }

    public String evalFieldSubstitutionAfterCommandSubstitution(String commandResult, NshContext context) {

        String IFS = context.vars().get("IFS", " \t\n");
        if (!IFS.isEmpty()) {
            //https://pubs.opengroup.org/onlinepubs/9699919799/utilities/V3_chap02.html#tag_18_06_03
            //https://unix.stackexchange.com/questions/164508/why-do-newline-characters-get-lost-when-using-command-substitution
            StringTokenizer st = new StringTokenizer(commandResult, IFS);
            StringBuilder sb = new StringBuilder();
            while (st.hasMoreTokens()) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(st.nextToken());
            }
            return sb.toString();
        }
        return commandResult;
    }

}
