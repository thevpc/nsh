package net.thevpc.nsh.options.autocomplete;

public class NshAutoCompleteCandidate {
    private String value;
    private String display;

    public NshAutoCompleteCandidate(String value) {
        this.value = value;
        this.display = value;
    }

    public NshAutoCompleteCandidate(String value, String display) {
        this.value = value;
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }

    public String getValue() {
        return value;
    }
}
