/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nsh.cmd.impl.util.filter;

import net.thevpc.nsh.cmd.impl.util.WindowFilter;
import net.thevpc.nsh.cmd.impl.util.NNumberedObject;
import net.thevpc.nuts.util.NStringUtils;

/**
 *
 * @author vpc
 */
public class StartsWithWindowFilter implements WindowFilter<NNumberedObject<String>> {

    private final String match;
    private final String matchString;
    private final boolean caseSensitive;

    public StartsWithWindowFilter(String match, boolean caseSensitive) {
        this.matchString = match;
        this.match = caseSensitive ? match : match.toLowerCase();
        this.caseSensitive = caseSensitive;
    }

    @Override
    public boolean accept(NNumberedObject<String> line) {
        String text = line.getObject();
        if (!caseSensitive) {
            text = text.toLowerCase();
        }
        return text.startsWith(match);
    }

    @Override
    public WindowFilter<NNumberedObject<String>> copy() {
        //immutable
        return this;
    }

    @Override
    public String toString() {
        if (caseSensitive) {
            return "StartsWith(" + NStringUtils.formatStringLiteral(matchString) + ')';
        } else {
            return "StartsWith(" + NStringUtils.formatStringLiteral(matchString) + ')';
        }
    }

}
