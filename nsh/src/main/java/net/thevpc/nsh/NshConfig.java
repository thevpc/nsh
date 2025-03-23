/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]  
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nsh;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nsh.cmd.resolver.NshCommandTypeResolver;
import net.thevpc.nsh.cmd.NshBuiltin;
import net.thevpc.nsh.err.NshErrorHandler;
import net.thevpc.nsh.eval.NshEvaluator;
import net.thevpc.nsh.history.NshHistory;
import net.thevpc.nsh.options.NshOptionsParser;
import net.thevpc.nsh.sys.NshExternalExecutor;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author vpc
 */
public class NshConfig {

    private NId appId;
    private String[] args;
    private String serviceName;
    private NshOptionsParser shellOptionsParser;
    private NshEvaluator evaluator;
    private NshCommandTypeResolver commandTypeResolver;
    private NshErrorHandler errorHandler;
    private NshExternalExecutor externalExecutor;
    private NshHistory history;
    private Predicate<NshBuiltin> builtinFilter;

    /**
     * defaults to true
     */
    private Boolean includeCoreBuiltins;
    /**
     * defaults to false
     */
    private Boolean includeDefaultBuiltins;

    /**
     * default false
     */
    private Boolean includeExternalExecutor;
    private Supplier<NMsg> headerMessageSupplier;

    public String[] getArgs() {
        return args;
    }

    public NshConfig setArgs(String... args) {
        this.args = args;
        return this;
    }

    public String getServiceName() {
        return serviceName;
    }

    public NshConfig setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public NshOptionsParser getOptionsParser() {
        return shellOptionsParser;
    }

    public NshConfig setShellOptionsParser(NshOptionsParser shellOptionsParser) {
        this.shellOptionsParser = shellOptionsParser;
        return this;
    }

    public NshEvaluator getEvaluator() {
        return evaluator;
    }

    public NshConfig setEvaluator(NshEvaluator evaluator) {
        this.evaluator = evaluator;
        return this;
    }

    public NshCommandTypeResolver getCommandTypeResolver() {
        return commandTypeResolver;
    }

    public NshConfig setCommandTypeResolver(NshCommandTypeResolver commandTypeResolver) {
        this.commandTypeResolver = commandTypeResolver;
        return this;
    }

    public NshErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public NshConfig setErrorHandler(NshErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public NshExternalExecutor getExternalExecutor() {
        return externalExecutor;
    }

    public NshConfig setExternalExecutor(NshExternalExecutor externalExecutor) {
        this.externalExecutor = externalExecutor;
        return this;
    }

    public NshHistory getHistory() {
        return history;
    }

    public NshConfig setHistory(NshHistory history) {
        this.history = history;
        return this;
    }

    public NId getAppId() {
        return appId;
    }

    public NshConfig setAppId(NId appId) {
        this.appId = appId;
        return this;
    }

    public Predicate<NshBuiltin> getBuiltinFilter() {
        return builtinFilter;
    }

    public NshConfig setBuiltinFilter(Predicate<NshBuiltin> builtinFilter) {
        this.builtinFilter = builtinFilter;
        return this;
    }

    public Supplier<NMsg> getHeaderMessageSupplier() {
        return headerMessageSupplier;
    }

    public NshConfig setHeaderMessageSupplier(Supplier<NMsg> headerMessageSupplier) {
        this.headerMessageSupplier = headerMessageSupplier;
        return this;
    }

    public Boolean getIncludeCoreBuiltins() {
        return includeCoreBuiltins;
    }

    public NshConfig setIncludeCoreBuiltins(Boolean includeCoreBuiltins) {
        this.includeCoreBuiltins = includeCoreBuiltins;
        return this;
    }

    public Boolean getIncludeDefaultBuiltins() {
        return includeDefaultBuiltins;
    }

    public NshConfig setIncludeDefaultBuiltins(Boolean includeDefaultBuiltins) {
        this.includeDefaultBuiltins = includeDefaultBuiltins;
        return this;
    }

    public Boolean getIncludeExternalExecutor() {
        return includeExternalExecutor;
    }

    public NshConfig setIncludeExternalExecutor(Boolean includeExternalExecutor) {
        this.includeExternalExecutor = includeExternalExecutor;
        return this;
    }
}
