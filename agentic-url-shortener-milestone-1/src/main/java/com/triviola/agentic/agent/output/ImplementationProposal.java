package com.triviola.agentic.agent.output;

import java.util.List;

public record ImplementationProposal(String summary, List<FileChange> fileChanges, List<String> risks,
                                     List<AiDecision> decisions) implements AgentOutput {
    @Override public boolean successful() { return true; }
}
