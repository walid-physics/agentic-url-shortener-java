package com.triviola.agentic.workflow;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "decision_records", indexes =
        @Index(name = "idx_decision_workflow_time", columnList = "workflow_id,created_at"))
public class DecisionRecordEntity {
    @Id @GeneratedValue private UUID id;
    @Column(nullable = false) private UUID workflowId;
    @Column(nullable = false, length = 100) private String taskKey;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private AgentType sourceAgent;
    @Column(nullable = false, length = 4_000) private String decision;
    @Column(nullable = false, length = 4_000) private String rationale;
    @Column(nullable = false) private Instant createdAt;

    protected DecisionRecordEntity() {}

    public DecisionRecordEntity(UUID workflowId, String taskKey, AgentType sourceAgent,
                                String decision, String rationale) {
        this.workflowId = workflowId;
        this.taskKey = taskKey;
        this.sourceAgent = sourceAgent;
        this.decision = decision;
        this.rationale = rationale;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getWorkflowId() { return workflowId; }
    public String getTaskKey() { return taskKey; }
    public AgentType getSourceAgent() { return sourceAgent; }
    public String getDecision() { return decision; }
    public String getRationale() { return rationale; }
    public Instant getCreatedAt() { return createdAt; }
}
