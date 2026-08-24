package com.triviola.agentic.agent.output;

import java.util.List;

public record RequirementAnalysis(String summary, List<String> ambiguities, List<String> assumptions,
                                  List<String> acceptanceCriteria, List<String> risks,
                                  List<AiDecision> decisions) implements AgentOutput {
    @Override public boolean successful() { return true; }
}
