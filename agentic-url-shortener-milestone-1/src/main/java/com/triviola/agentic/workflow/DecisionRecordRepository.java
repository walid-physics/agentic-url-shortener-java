package com.triviola.agentic.workflow;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DecisionRecordRepository extends JpaRepository<DecisionRecordEntity, UUID> {
    List<DecisionRecordEntity> findByWorkflowIdOrderByCreatedAtAsc(UUID workflowId);
}
