package com.triviola.agentic.agent.output;

import java.util.List;

public interface AgentOutput {
    String summary();
    boolean successful();
    List<AiDecision> decisions();
}
