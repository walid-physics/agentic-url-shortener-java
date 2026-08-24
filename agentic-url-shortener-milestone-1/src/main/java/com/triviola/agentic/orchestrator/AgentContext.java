package com.triviola.agentic.orchestrator;

import com.triviola.agentic.workflow.AgentType;
import java.util.Map;
import java.util.UUID;

public record AgentContext(UUID workflowId, String taskKey, AgentType agentType,
                           String requirement, Map<String, String> upstreamArtifacts, int attempt) {
    public AgentContext {
        upstreamArtifacts = Map.copyOf(upstreamArtifacts);
    }
}
