package net.thevpc.nsh.cmd;

import java.util.function.Supplier;

public abstract class NshBuiltinCore extends NshBuiltinBase {
    public NshBuiltinCore(String name, int score, Class<?> optionsSupplier) {
        super(name, score, optionsSupplier);
    }

    public NshBuiltinCore(String name, Class<?> optionsSupplier) {
        super(name, optionsSupplier);
    }

    public NshBuiltinCore(String name, int score, Supplier<?> optionsSupplier) {
        super(name, score, optionsSupplier);
    }

    public NshBuiltinCore(String name, Supplier<?> optionsSupplier) {
        super(name, optionsSupplier);
    }
}
