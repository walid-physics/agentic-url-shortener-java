package com.triviola.agentic.workflow;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "workflow_tasks", uniqueConstraints =
        @UniqueConstraint(name = "uk_workflow_task_key", columnNames = {"workflow_id", "task_key"}))
public class WorkflowTaskEntity {
    @Id @GeneratedValue private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_id", nullable = false) private WorkflowEntity workflow;
    @Column(nullable = false) private int sequenceNumber;
    @Column(name = "task_key", nullable = false, length = 100) private String taskKey;
    @Column(nullable = false, length = 240) private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private AgentType agentType;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private TaskStatus status;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private RiskLevel riskLevel;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "workflow_task_dependencies", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "dependency_key", nullable = false, length = 100)
    private Set<String> dependencies = new LinkedHashSet<>();

    private int retryCount;
    private int maxRetries;
    private boolean approvalRequired;
    private boolean approved;
    @Column(length = 4_000) private String failureReason;
    @Column(length = 100_000) private String outputArtifact;
    private Instant startedAt;
    private Instant completedAt;

    protected WorkflowTaskEntity() {}

    public WorkflowTaskEntity(int sequenceNumber, String taskKey, String name, AgentType agentType,
                              RiskLevel riskLevel, Set<String> dependencies, int maxRetries,
                              boolean approvalRequired) {
        this.sequenceNumber = sequenceNumber;
        this.taskKey = taskKey;
        this.name = name;
        this.agentType = agentType;
        this.riskLevel = riskLevel;
        this.dependencies = new LinkedHashSet<>(dependencies);
        this.maxRetries = maxRetries;
        this.approvalRequired = approvalRequired;
        this.status = TaskStatus.PENDING;
    }

    void attachTo(WorkflowEntity workflow) { this.workflow = workflow; }
    public UUID getId() { return id; }
    public WorkflowEntity getWorkflow() { return workflow; }
    public int getSequenceNumber() { return sequenceNumber; }
    public String getTaskKey() { return taskKey; }
    public String getName() { return name; }
    public AgentType getAgentType() { return agentType; }
    public TaskStatus getStatus() { return status; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public Set<String> getDependencies() { return Set.copyOf(dependencies); }
    public int getRetryCount() { return retryCount; }
    public int getMaxRetries() { return maxRetries; }
    public boolean isApprovalRequired() { return approvalRequired; }
    public boolean isApproved() { return approved; }
    public String getFailureReason() { return failureReason; }
    public String getOutputArtifact() { return outputArtifact; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }

    public void markRunning() { status = TaskStatus.RUNNING; startedAt = Instant.now(); }
    public void waitForApproval() { status = TaskStatus.WAITING_APPROVAL; }
    public void approve() { approved = true; status = TaskStatus.PENDING; }
    public void reject(String reason) { status = TaskStatus.BLOCKED; failureReason = reason; completedAt = Instant.now(); }
    public void pass(String artifact) { status = TaskStatus.PASSED; outputArtifact = artifact; failureReason = null; completedAt = Instant.now(); }
    public boolean retry(String reason) {
        failureReason = reason;
        if (retryCount >= maxRetries) {
            status = TaskStatus.FAILED;
            completedAt = Instant.now();
            return false;
        }
        retryCount++;
        status = TaskStatus.PENDING;
        return true;
    }
    public void block() { status = TaskStatus.BLOCKED; completedAt = Instant.now(); }
}
