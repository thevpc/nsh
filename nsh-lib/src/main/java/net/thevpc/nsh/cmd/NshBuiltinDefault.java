package net.thevpc.nsh.cmd;

import java.util.function.Supplier;

public abstract class NshBuiltinDefault extends NshBuiltinBase {
    public NshBuiltinDefault(String name, Class<?> optionsSupplier) {
        super(name, optionsSupplier);
    }

    public NshBuiltinDefault(String name, Supplier<?> optionsSupplier) {
        super(name, optionsSupplier);
    }

}
