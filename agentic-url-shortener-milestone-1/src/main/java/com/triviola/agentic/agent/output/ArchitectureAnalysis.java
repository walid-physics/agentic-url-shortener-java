package com.triviola.agentic.agent.output;

import java.util.List;

public record ArchitectureAnalysis(String summary, List<String> affectedComponents,
                                   List<String> architecturalChanges, List<String> apiChanges,
                                   List<String> dataModelChanges, List<String> risks,
                                   List<AiDecision> decisions) implements AgentOutput {
    @Override public boolean successful() { return true; }
}
