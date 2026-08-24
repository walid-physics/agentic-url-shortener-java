package com.triviola.agentic.agent.output;

import java.util.List;

public record EngineeringPlan(String summary, List<PlannedTask> tasks, List<String> risks,
                              List<AiDecision> decisions) implements AgentOutput {
    @Override public boolean successful() { return true; }
}
