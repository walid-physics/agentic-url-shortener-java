package com.triviola.agentic.workflow;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface WorkflowEventRepository extends JpaRepository<WorkflowEventEntity, UUID> {
    List<WorkflowEventEntity> findByWorkflowIdOrderByOccurredAtAsc(UUID workflowId);
}
