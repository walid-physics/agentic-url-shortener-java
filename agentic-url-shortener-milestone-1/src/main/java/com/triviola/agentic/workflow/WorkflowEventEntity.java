package com.triviola.agentic.workflow;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workflow_events", indexes =
        @Index(name = "idx_event_workflow_time", columnList = "workflow_id,occurred_at"))
public class WorkflowEventEntity {
    @Id @GeneratedValue private UUID id;
    @Column(nullable = false) private UUID workflowId;
    @Column(length = 100) private String taskKey;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 50) private EventType eventType;
    @Column(nullable = false) private Instant occurredAt;
    @Column(nullable = false) private int attempt;
    private long durationMs;
    @Column(length = 64) private String inputHash;
    @Column(length = 64) private String outputHash;
    @Column(length = 8_000) private String details;

    protected WorkflowEventEntity() {}

    public WorkflowEventEntity(UUID workflowId, String taskKey, EventType eventType, int attempt,
                               long durationMs, String inputHash, String outputHash, String details) {
        this.workflowId = workflowId;
        this.taskKey = taskKey;
        this.eventType = eventType;
        this.attempt = attempt;
        this.durationMs = durationMs;
        this.inputHash = inputHash;
        this.outputHash = outputHash;
        this.details = details;
        this.occurredAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getWorkflowId() { return workflowId; }
    public String getTaskKey() { return taskKey; }
    public EventType getEventType() { return eventType; }
    public Instant getOccurredAt() { return occurredAt; }
    public int getAttempt() { return attempt; }
    public long getDurationMs() { return durationMs; }
    public String getInputHash() { return inputHash; }
    public String getOutputHash() { return outputHash; }
    public String getDetails() { return details; }
}
