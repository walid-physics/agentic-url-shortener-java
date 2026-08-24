package com.triviola.agentic.workflow;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "workflows")
public class WorkflowEntity {
    @Id private UUID id;
    @Column(nullable = false, length = 12_000) private String requirement;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private WorkflowStatus status;
    @Column(nullable = false) private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;
    @Version private long version;

    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.EAGER)
    @OrderBy("sequenceNumber ASC")
    private Set<WorkflowTaskEntity> tasks = new LinkedHashSet<>();

    protected WorkflowEntity() {}

    public WorkflowEntity(UUID id, String requirement) {
        this.id = id;
        this.requirement = requirement;
        this.status = WorkflowStatus.CREATED;
        this.createdAt = Instant.now();
    }

    public void addTask(WorkflowTaskEntity task) { task.attachTo(this); tasks.add(task); }
    public UUID getId() { return id; }
    public String getRequirement() { return requirement; }
    public WorkflowStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public long getVersion() { return version; }
    public List<WorkflowTaskEntity> getTasks() {
        return tasks.stream().sorted(Comparator.comparingInt(WorkflowTaskEntity::getSequenceNumber)).toList();
    }

    public void start() { status = WorkflowStatus.RUNNING; if (startedAt == null) startedAt = Instant.now(); }
    public void waitForApproval() { status = WorkflowStatus.WAITING_APPROVAL; }
    public void reject() { status = WorkflowStatus.REJECTED; completedAt = Instant.now(); }
    public void safeStop() { status = WorkflowStatus.SAFE_STOPPED; completedAt = Instant.now(); }
    public void complete() { status = WorkflowStatus.COMPLETED; completedAt = Instant.now(); }
}
