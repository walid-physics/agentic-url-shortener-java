package com.triviola.agentic.orchestrator;

import com.triviola.agentic.workflow.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WorkflowService {
    private final WorkflowRepository repository;
    private final WorkflowFactory factory;
    private final AuditService auditService;

    public WorkflowService(WorkflowRepository repository, WorkflowFactory factory, AuditService auditService) {
        this.repository = repository;
        this.factory = factory;
        this.auditService = auditService;
    }

    public WorkflowEntity create(String requirement) {
        WorkflowEntity saved = repository.save(factory.create(requirement));
        auditService.record(saved.getId(), null, EventType.WORKFLOW_CREATED, 0, 0,
                requirement, saved.getStatus().name(), "Workflow DAG created");
        return saved;
    }

    public WorkflowEntity get(UUID workflowId) {
        return repository.findById(workflowId).orElseThrow(() -> new WorkflowNotFoundException(workflowId));
    }

    public WorkflowEntity approve(UUID workflowId, String taskKey) {
        WorkflowEntity workflow = get(workflowId);
        WorkflowTaskEntity task = findTask(workflow, taskKey);
        if (task.getStatus() != TaskStatus.WAITING_APPROVAL) {
            throw new IllegalStateException("Task is not waiting for approval: " + taskKey);
        }
        task.approve();
        workflow.start();
        WorkflowEntity saved = repository.save(workflow);
        auditService.record(workflowId, taskKey, EventType.TASK_APPROVED, task.getRetryCount() + 1,
                0, "human approval", "approved", "Human approved high-impact task");
        return saved;
    }

    public WorkflowEntity reject(UUID workflowId, String taskKey, String reason) {
        WorkflowEntity workflow = get(workflowId);
        WorkflowTaskEntity task = findTask(workflow, taskKey);
        if (task.getStatus() != TaskStatus.WAITING_APPROVAL) {
            throw new IllegalStateException("Task is not waiting for approval: " + taskKey);
        }
        task.reject(reason);
        workflow.reject();
        WorkflowEntity saved = repository.save(workflow);
        auditService.record(workflowId, taskKey, EventType.TASK_REJECTED, task.getRetryCount() + 1,
                0, "human review", reason, "Human rejected task");
        return saved;
    }

    private WorkflowTaskEntity findTask(WorkflowEntity workflow, String taskKey) {
        return workflow.getTasks().stream()
                .filter(task -> task.getTaskKey().equals(taskKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown task: " + taskKey));
    }
}
