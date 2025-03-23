package net.thevpc.nsh.cmd.impl.util;

public class NNumberedObject<T> {
    private T object;
    private long number;

    public NNumberedObject(T line, long number) {
        this.object = line;
        this.number = number;
    }

    public T getObject() {
        return object;
    }

    public long getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return "[" + number + "] " + object;
    }
}
