/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nsh.cmd.resolver;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nsh.eval.NshContext;

/**
 *
 * @author thevpc
 */
public class DefaultNshCommandTypeResolver implements NshCommandTypeResolver {

    @Override
    public NshCommandResolution type(String item, NshContext context) {
        String a = context.aliases().get(item);
        if (a != null) {
            return new NshCommandResolution(item, "path", a, item + " is aliased to " + a);
        }
        NPath path = NPath.of(item).toAbsolute(context.getDirectory());
        if (path.exists()) {
            return new NshCommandResolution(item, "path", path.toString(), item + " is " + path);
        }
        return null;
    }

}
