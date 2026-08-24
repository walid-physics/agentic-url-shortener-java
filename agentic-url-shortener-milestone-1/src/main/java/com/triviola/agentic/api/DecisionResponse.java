package com.triviola.agentic.api;

import com.triviola.agentic.workflow.AgentType;
import com.triviola.agentic.workflow.DecisionRecordEntity;
import java.time.Instant;
import java.util.UUID;

public record DecisionResponse(UUID id, String taskKey, AgentType sourceAgent, String decision,
                               String rationale, Instant createdAt) {
    static DecisionResponse from(DecisionRecordEntity decision) {
        return new DecisionResponse(decision.getId(), decision.getTaskKey(), decision.getSourceAgent(),
                decision.getDecision(), decision.getRationale(), decision.getCreatedAt());
    }
}
