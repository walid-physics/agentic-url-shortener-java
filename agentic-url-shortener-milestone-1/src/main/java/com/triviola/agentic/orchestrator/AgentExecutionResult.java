package com.triviola.agentic.orchestrator;

import java.util.List;

public record AgentExecutionResult(boolean successful, String summary, String outputArtifact,
                                   List<DecisionProposal> decisions) {
    public AgentExecutionResult {
        decisions = decisions == null ? List.of() : List.copyOf(decisions);
    }

    public static AgentExecutionResult success(String summary, String outputArtifact,
                                                List<DecisionProposal> decisions) {
        return new AgentExecutionResult(true, summary, outputArtifact, decisions);
    }

    public static AgentExecutionResult failure(String reason) {
        return new AgentExecutionResult(false, reason, "", List.of());
    }
}
