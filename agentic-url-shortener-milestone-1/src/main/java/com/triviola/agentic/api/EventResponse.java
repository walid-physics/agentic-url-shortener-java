package com.triviola.agentic.api;

import com.triviola.agentic.workflow.EventType;
import com.triviola.agentic.workflow.WorkflowEventEntity;
import java.time.Instant;
import java.util.UUID;

public record EventResponse(UUID id, String taskKey, EventType type, Instant occurredAt,
                            int attempt, long durationMs, String inputHash, String outputHash,
                            String details) {
    static EventResponse from(WorkflowEventEntity event) {
        return new EventResponse(event.getId(), event.getTaskKey(), event.getEventType(),
                event.getOccurredAt(), event.getAttempt(), event.getDurationMs(), event.getInputHash(),
                event.getOutputHash(), event.getDetails());
    }
}
