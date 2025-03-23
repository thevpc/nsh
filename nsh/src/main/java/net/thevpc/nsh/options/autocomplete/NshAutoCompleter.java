package net.thevpc.nsh.options.autocomplete;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.DefaultNArgCandidate;
import net.thevpc.nuts.cmdline.NArgCandidate;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineAutoCompleteResolver;

import net.thevpc.nsh.cmd.NshBuiltin;
import net.thevpc.nsh.eval.NshContext;
import net.thevpc.nuts.util.NBlankable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NshAutoCompleter implements NCmdLineAutoCompleteResolver {
    private NWorkspace workspace;

    public NshAutoCompleter(NWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public List<NArgCandidate> resolveCandidates(NCmdLine cmdLine, int wordIndex) {
        List<NArgCandidate> candidates = new ArrayList<>();
        NSession session=workspace.currentSession();
        NshContext fileContext = (NshContext) NWorkspace.of().getProperties().get(NshContext.class.getName());

        if (wordIndex == 0) {
            for (NshBuiltin command : fileContext.builtins().getAll()) {
                candidates.add(new DefaultNArgCandidate(command.getName()));
            }
        } else {
            List<String> autoCompleteWords = new ArrayList<>(Arrays.asList(cmdLine.toStringArray()));
            int x = cmdLine.getCommandName().length();

            List<NshAutoCompleteCandidate> autoCompleteCandidates
                    = fileContext.resolveAutoCompleteCandidates(cmdLine.getCommandName(), autoCompleteWords, wordIndex, cmdLine.toString());
            for (Object cmdCandidate0 : autoCompleteCandidates) {
                NshAutoCompleteCandidate cmdCandidate = (NshAutoCompleteCandidate) cmdCandidate0;
                if (cmdCandidate != null) {
                    String value = cmdCandidate.getValue();
                    if (!NBlankable.isBlank(value)) {
                        String display = cmdCandidate.getDisplay();
                        if (NBlankable.isBlank(display)) {
                            display = value;
                        }
                        candidates.add(new DefaultNArgCandidate(value,display));
                    }
                }
            }
        }
        return candidates;
    }
}
