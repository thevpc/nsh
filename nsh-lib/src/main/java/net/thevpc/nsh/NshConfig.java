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

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nsh.cmd.resolver.NshCommandTypeResolver;
import net.thevpc.nsh.cmd.NshBuiltin;
import net.thevpc.nsh.err.NshErrorHandler;
import net.thevpc.nsh.eval.NshEvaluator;
import net.thevpc.nsh.history.NshHistory;
import net.thevpc.nsh.options.NshOptions;
import net.thevpc.nsh.options.NshOptionsParser;
import net.thevpc.nsh.sys.NshExternalExecutor;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Shell engine construction and dependency-injection configuration.
 * <p>
 * {@code NshConfig} is used at shell initialization time (passed to {@link Nsh#Nsh(NshConfig)})
 * to wire the shell engine's architecture and subsystems:
 * <ul>
 *   <li>Pluggable components: {@link NshEvaluator}, {@link NshCommandTypeResolver},
 *       {@link NshErrorHandler}, {@link NshExternalExecutor}, {@link NshHistory}</li>
 *   <li>Builtin registry configuration: core builtins, default builtins, and custom filters</li>
 *   <li>Application identity: {@link NId appId} and {@code serviceName} (used for history files and properties)</li>
 *   <li>Command-line options: raw {@code args}, a custom {@link NshOptionsParser}, or pre-configured {@link NshOptions}</li>
 * </ul>
 *
 * <h3>Difference between {@code NshConfig} and {@code NshOptions}</h3>
 * <ul>
 *   <li><strong>{@code NshConfig}</strong> = <em>Engine Wiring & Setup Configuration</em>.
 *       Configures how the NSH engine itself is built, which components are injected, and which
 *       features are enabled. It is configured programmatically prior to shell startup.</li>
 *   <li><strong>{@link NshOptions}</strong> = <em>Session Runtime Flags & POSIX/Bash Options</em>.
 *       Represents the command-line flags and runtime session state (such as {@code -c}, {@code -i},
 *       {@code --bash}, {@code --posix}, {@code -x}, {@code -e}, startup scripts, and positional arguments).
 *       Can be modified dynamically at runtime (e.g. via {@code set -x}).</li>
 * </ul>
 *
 * @author vpc
 * @see Nsh
 * @see NshOptions
 */
public class NshConfig implements Cloneable {

    private NId appId;
    private String[] args;
    private String serviceName;
    private NshOptions options;
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
     * defaults to true
     */
    private Boolean includeHistory;

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

    /**
     * Pre-configured shell invocation and runtime options.
     * If not provided, options will be parsed from {@link #getArgs()}
     * using {@link #getOptionsParser()}.
     *
     * @return pre-configured options, or null if unconfigured
     */
    public NshOptions getOptions() {
        return options;
    }

    /**
     * Sets pre-configured shell invocation and runtime options.
     *
     * @param options pre-configured options
     * @return this instance
     */
    public NshConfig setOptions(NshOptions options) {
        this.options = options;
        return this;
    }

    /**
     * The application or service identity name (e.g. {@code "nsh"}).
     * Used for the history filename ({@code <serviceName>.history}) and
     * default shell prompt title. Defaults to {@code appId.artifactId()}.
     *
     * @return service/application name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Sets the application or service identity name.
     *
     * @param serviceName service/application name
     * @return this instance
     */
    public NshConfig setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    /**
     * Alias for {@link #getServiceName()}, representing the application/shell name.
     *
     * @return application name
     */
    public String getAppName() {
        return getServiceName();
    }

    /**
     * Alias for {@link #setServiceName(String)}, representing the application/shell name.
     *
     * @param appName application name
     * @return this instance
     */
    public NshConfig setAppName(String appName) {
        return setServiceName(appName);
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

    public Boolean getIncludeHistory() {
        return includeHistory;
    }

    public NshConfig setIncludeHistory(Boolean includeHistory) {
        this.includeHistory = includeHistory;
        return this;
    }

    public NshConfig copy() {
        return clone();
    }

    @Override
    protected NshConfig clone() {
        try {
            NshConfig other = (NshConfig) super.clone();
            if (this.options != null) {
                other.options = this.options.copy();
            }
            if (this.args != null) {
                other.args = this.args.clone();
            }
            return other;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

}
