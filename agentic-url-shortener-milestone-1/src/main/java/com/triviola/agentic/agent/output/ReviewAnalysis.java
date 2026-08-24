package com.triviola.agentic.agent.output;

import java.util.List;

public record ReviewAnalysis(boolean releaseReady, String summary, List<String> blockers,
                             List<String> followUps, List<AiDecision> decisions) implements AgentOutput {
    @Override public boolean successful() { return releaseReady; }
}
