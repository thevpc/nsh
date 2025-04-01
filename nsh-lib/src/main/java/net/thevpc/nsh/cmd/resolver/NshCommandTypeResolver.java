/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nsh.cmd.resolver;

import net.thevpc.nsh.eval.NshContext;

/**
 *
 * @author thevpc
 */
public interface NshCommandTypeResolver {

    NshCommandResolution type(String path0, NshContext context);

}
