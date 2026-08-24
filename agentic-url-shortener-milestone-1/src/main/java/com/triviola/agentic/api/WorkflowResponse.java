package com.triviola.agentic.api;

import com.triviola.agentic.workflow.WorkflowEntity;
import com.triviola.agentic.workflow.WorkflowStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WorkflowResponse(UUID id, String requirement, WorkflowStatus status, long version,
                               Instant createdAt, Instant startedAt, Instant completedAt,
                               List<TaskResponse> tasks) {
    public static WorkflowResponse from(WorkflowEntity workflow) {
        return new WorkflowResponse(workflow.getId(), workflow.getRequirement(), workflow.getStatus(),
                workflow.getVersion(), workflow.getCreatedAt(), workflow.getStartedAt(), workflow.getCompletedAt(),
                workflow.getTasks().stream().map(TaskResponse::from).toList());
    }
}
