package com.triviola.agentic.orchestrator;

import com.triviola.agentic.workflow.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DecisionService {
    private final DecisionRecordRepository repository;
    private final AuditService auditService;

    public DecisionService(DecisionRecordRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    public void recordAll(UUID workflowId, String taskKey, AgentType source,
                          List<DecisionProposal> proposals) {
        for (DecisionProposal proposal : proposals) {
            repository.save(new DecisionRecordEntity(workflowId, taskKey, source,
                    proposal.decision(), proposal.rationale()));
            auditService.record(workflowId, taskKey, EventType.DECISION_RECORDED, 0, 0,
                    proposal.rationale(), proposal.decision(), "Decision recorded by " + source);
        }
    }
}
