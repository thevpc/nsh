package net.thevpc.nsh.cmd;

import java.util.function.Supplier;

public abstract class NshBuiltinDefault extends NshBuiltinBase {
    public NshBuiltinDefault(String name, int supportLevel, Class<?> optionsSupplier) {
        super(name, supportLevel, optionsSupplier);
    }

    public NshBuiltinDefault(String name, Class<?> optionsSupplier) {
        super(name, optionsSupplier);
    }

    public NshBuiltinDefault(String name, int supportLevel, Supplier<?> optionsSupplier) {
        super(name, supportLevel, optionsSupplier);
    }

    public NshBuiltinDefault(String name, Supplier<?> optionsSupplier) {
        super(name, optionsSupplier);
    }
}
