/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nsh.cmd.alias;

import java.util.Set;

/**
 *
 * @author thevpc
 */
public interface NshAliasManager {

    String get(String name);

    Set<String> getAll();

    void set(String key, String value);
    
}
