package net.thevpc.nsh.parser;

import net.thevpc.nsh.parser.nodes.NshCommandNode;
import net.thevpc.nsh.parser.nodes.NshNode;
import net.thevpc.nsh.parser.ctx.DefaultContext;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NshParser {
    private StrReader strReader = new StrReader();
    private DefaultLexer lexer = new DefaultLexer(this);
    private Yaccer yaccer = new Yaccer(this.lexer);

    public NshParser(Reader reader) {
        strReader.reader = reader;
        lexer.ctx.push(new DefaultContext(this));
    }

    public static NshParser fromString(String s) {
        return new NshParser(new StringReader(s == null ? "" : s));
    }

    public static NshParser fromInputStream(InputStream s) {
        return new NshParser(s==null?new StringReader("") : new InputStreamReader(s));
    }

    public StrReader strReader() {
        return strReader;
    }

    public DefaultLexer lexer() {
        return lexer;
    }

    public Yaccer yaccer() {
        return yaccer;
    }


    public NshNode parse() {
        return yaccer().readScript();
    }

    public static NshCommandNode createCommandNode(String[] args) {
        List<Yaccer.Argument> args2 = new ArrayList<>();
        for (String arg : args) {
            args2.add(new Yaccer.Argument(
                    Arrays.asList(
                            new Yaccer.TokenNode(
                                    new Token(
                                            "WORD",
                                            arg,
                                            arg
                                    )
                            )
                    )
            ));
        }
        return new Yaccer.ArgumentsLine(args2);
    }
}
