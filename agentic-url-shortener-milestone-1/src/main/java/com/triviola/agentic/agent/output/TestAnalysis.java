package com.triviola.agentic.agent.output;

import java.util.List;

public record TestAnalysis(boolean successful, String summary, List<String> findings,
                           List<String> recommendedFixes, List<AiDecision> decisions) implements AgentOutput {}
