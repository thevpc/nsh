package net.thevpc.nsh.options.autocomplete;

import net.thevpc.nuts.cmdline.*;

import net.thevpc.nsh.cmd.NshBuiltin;
import net.thevpc.nsh.eval.NshContext;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.util.NBlankable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NshCompleter implements NArgCompleteResolver {
    public NshCompleter() {
    }

    @Override
    public NArgCompleteResult resolveCandidates(NCmdLine cmdLine, NArgCompletePos pos) {
        List<NArgCompleteCandidate> candidates = new ArrayList<>();
        NshContext fileContext = (NshContext) NWorkspace.of().properties().get(NshContext.class.getName());
        if (pos.wordIndex() == 0) {
            for (NshBuiltin command : fileContext.builtins().getAll()) {
                candidates.add(NArgCompleteCandidate.of(command.getName()));
            }
        } else {
            List<String> autoCompleteWords = new ArrayList<>(Arrays.asList(cmdLine.toStringArray()));
            int x = cmdLine.commandName().length();

            List<NshAutoCompleteCandidate> autoCompleteCandidates
                    = fileContext.resolveAutoCompleteCandidates(cmdLine.commandName(), autoCompleteWords, cmdLine.toString(), pos);
            for (Object cmdCandidate0 : autoCompleteCandidates) {
                NshAutoCompleteCandidate cmdCandidate = (NshAutoCompleteCandidate) cmdCandidate0;
                if (cmdCandidate != null) {
                    String value = cmdCandidate.getValue();
                    if (!NBlankable.isBlank(value)) {
                        String display = cmdCandidate.getDisplay();
                        if (NBlankable.isBlank(display)) {
                            display = value;
                        }
                        candidates.add(NArgCompleteCandidate.of(value,display));
                    }
                }
            }
        }
        return NArgCompleteResult.of(candidates,null);
    }
}
