package com.triviola.agentic.orchestrator;

import com.triviola.agentic.workflow.EventType;
import com.triviola.agentic.workflow.WorkflowEventEntity;
import com.triviola.agentic.workflow.WorkflowEventRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditService {
    private final WorkflowEventRepository repository;

    public AuditService(WorkflowEventRepository repository) { this.repository = repository; }

    public void record(UUID workflowId, String taskKey, EventType type, int attempt,
                       long durationMs, String input, String output, String details) {
        repository.save(new WorkflowEventEntity(workflowId, taskKey, type, attempt, durationMs,
                Hashing.sha256(input), Hashing.sha256(output), details));
    }
}
