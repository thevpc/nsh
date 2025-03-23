package net.thevpc.nsh.parser;

public abstract class AbstractContext implements Context {
    protected final NshParser reader;

    public AbstractContext(NshParser reader) {
        this.reader = reader;
    }

}
