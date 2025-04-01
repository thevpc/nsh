package net.thevpc.nsh.cmd.impl.util;

import java.util.List;

public class WindowObject<T> {
    private List<T> items;
    private int pivotIndex;

    public WindowObject(List<T> items, int pivotIndex) {
        this.items = items;
        this.pivotIndex = pivotIndex;
    }

    public List<T> getItems() {
        return items;
    }

    public int getPivotIndex() {
        return pivotIndex;
    }
}
