/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
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
 * <br>
 * ====================================================================
 */
package net.thevpc.nsh.cmd;

import net.thevpc.nuts.cmdline.NArgCompleteCandidate;
import net.thevpc.nuts.cmdline.NArgCompleteFlag;
import net.thevpc.nuts.cmdline.NArgCompletePos;
import net.thevpc.nuts.cmdline.NArgCompleteResult;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Base (Abstract) implementation of NutsCommandAutoComplete
 *
 * @author thevpc
 * @app.category Command Line
 * @since 0.5.5
 */
public abstract class NCmdLineCompleteBase implements NCmdLineComplete {

    /**
     * candidates map
     */
    private final LinkedHashMap<String, NArgCompleteCandidate> candidates = new LinkedHashMap<>();
    private final LinkedHashSet<NArgCompleteFlag> flags = new LinkedHashSet<>();
    private final NArgCompletePos currentPos;

    public NCmdLineCompleteBase(NArgCompletePos currentPos) {
        this.currentPos = NAssert.requireNamedNonNull(currentPos, "currentPos");
    }

    @Override
    public NArgCompletePos currentPos() {
        return currentPos;
    }

    /**
     * possible candidates
     *
     * @return possible candidates
     */
    @Override
    public List<NArgCompleteCandidate> candidates() {
        return new ArrayList<>(candidates.values());
    }

    @Override
    public NArgCompleteResult result() {
        return NArgCompleteResult.of(candidates(), flags());
    }

    @Override
    public List<NArgCompleteFlag> flags() {
        return new ArrayList<>(flags);
    }

    /**
     * add candidate
     *
     * @param value candidate
     */
    @Override
    public void addCandidate(NArgCompleteCandidate value) {
        if (value != null && !NStringUtils.strip(value.value()).isEmpty()) {
            addCandidatesImpl(value);
        }
    }

    @Override
    public void addFlag(NArgCompleteFlag value) {
        if (value != null) {
            flags.add(value);
        }
    }

    /**
     * simple add candidates implementation
     *
     * @param value candidate
     * @return {@code this} instance
     */
    protected NArgCompleteCandidate addCandidatesImpl(NArgCompleteCandidate value) {
        return candidates.put(value.value(), value);
    }

}
