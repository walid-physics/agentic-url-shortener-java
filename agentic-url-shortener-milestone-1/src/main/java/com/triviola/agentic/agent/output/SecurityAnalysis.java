package com.triviola.agentic.agent.output;

import java.util.List;

public record SecurityAnalysis(boolean successful, String summary, List<String> findings,
                               List<String> mitigations, List<AiDecision> decisions) implements AgentOutput {}
