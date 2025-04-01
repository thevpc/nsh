package net.thevpc.nsh.cmd;

import java.util.function.Supplier;

public abstract class NshBuiltinCore extends NshBuiltinBase {
    public NshBuiltinCore(String name, int supportLevel, Class<?> optionsSupplier) {
        super(name, supportLevel, optionsSupplier);
    }

    public NshBuiltinCore(String name, Class<?> optionsSupplier) {
        super(name, optionsSupplier);
    }

    public NshBuiltinCore(String name, int supportLevel, Supplier<?> optionsSupplier) {
        super(name, supportLevel, optionsSupplier);
    }

    public NshBuiltinCore(String name, Supplier<?> optionsSupplier) {
        super(name, optionsSupplier);
    }
}
