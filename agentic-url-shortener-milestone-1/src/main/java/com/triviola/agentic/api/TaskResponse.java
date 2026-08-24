package com.triviola.agentic.api;

import com.triviola.agentic.workflow.*;
import java.time.Instant;
import java.util.Set;

public record TaskResponse(String key, String name, AgentType agent, TaskStatus status,
                           RiskLevel risk, Set<String> dependencies, int retryCount,
                           int maxRetries, boolean approvalRequired, boolean approved,
                           String failureReason, String outputArtifact,
                           Instant startedAt, Instant completedAt) {
    static TaskResponse from(WorkflowTaskEntity task) {
        return new TaskResponse(task.getTaskKey(), task.getName(), task.getAgentType(), task.getStatus(),
                task.getRiskLevel(), task.getDependencies(), task.getRetryCount(), task.getMaxRetries(),
                task.isApprovalRequired(), task.isApproved(), task.getFailureReason(), task.getOutputArtifact(),
                task.getStartedAt(), task.getCompletedAt());
    }
}
