package net.thevpc.nsh.parser;

public interface Lexer {
    boolean skipWhites();

    Token peekToken();

    Token nextToken();
}
