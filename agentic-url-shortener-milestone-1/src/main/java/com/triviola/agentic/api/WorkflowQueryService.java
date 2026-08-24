package com.triviola.agentic.api;

import com.triviola.agentic.orchestrator.WorkflowService;
import com.triviola.agentic.workflow.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class WorkflowQueryService {
    private final WorkflowService workflowService;
    private final WorkflowEventRepository eventRepository;
    private final DecisionRecordRepository decisionRepository;

    public WorkflowQueryService(WorkflowService workflowService, WorkflowEventRepository eventRepository,
                                DecisionRecordRepository decisionRepository) {
        this.workflowService = workflowService;
        this.eventRepository = eventRepository;
        this.decisionRepository = decisionRepository;
    }

    public List<EventResponse> events(UUID workflowId) {
        workflowService.get(workflowId);
        return eventRepository.findByWorkflowIdOrderByOccurredAtAsc(workflowId).stream()
                .map(EventResponse::from).toList();
    }

    public List<DecisionResponse> decisions(UUID workflowId) {
        workflowService.get(workflowId);
        return decisionRepository.findByWorkflowIdOrderByCreatedAtAsc(workflowId).stream()
                .map(DecisionResponse::from).toList();
    }

    public WorkflowMetricsResponse metrics(UUID workflowId) {
        WorkflowEntity workflow = workflowService.get(workflowId);
        List<WorkflowEventEntity> events = eventRepository.findByWorkflowIdOrderByOccurredAtAsc(workflowId);
        long passed = workflow.getTasks().stream().filter(task -> task.getStatus() == TaskStatus.PASSED).count();
        double successRate = workflow.getTasks().isEmpty() ? 0 : (double) passed / workflow.getTasks().size();
        int retries = (int) events.stream().filter(event -> event.getEventType() == EventType.RETRY_SCHEDULED).count();
        int rollbacks = (int) events.stream().filter(event -> event.getEventType() == EventType.ROLLBACK_COMPLETED).count();
        int approvals = (int) events.stream().filter(event -> event.getEventType() == EventType.TASK_APPROVED).count();
        Instant end = workflow.getCompletedAt() == null ? Instant.now() : workflow.getCompletedAt();
        Instant start = workflow.getStartedAt() == null ? workflow.getCreatedAt() : workflow.getStartedAt();
        long latency = Math.max(0, Duration.between(start, end).toMillis());
        return new WorkflowMetricsResponse(workflow.getStatus(), successRate, retries, rollbacks,
                approvals, latency, calculateMttr(events));
    }

    private Long calculateMttr(List<WorkflowEventEntity> events) {
        Map<String, Instant> failures = new HashMap<>();
        List<Long> recoveries = new ArrayList<>();
        for (WorkflowEventEntity event : events) {
            if (event.getTaskKey() == null) continue;
            if (event.getEventType() == EventType.TASK_FAILED) {
                failures.putIfAbsent(event.getTaskKey(), event.getOccurredAt());
            } else if (event.getEventType() == EventType.TASK_PASSED) {
                Instant failedAt = failures.remove(event.getTaskKey());
                if (failedAt != null) recoveries.add(Duration.between(failedAt, event.getOccurredAt()).toMillis());
            }
        }
        if (recoveries.isEmpty()) return null;
        return Math.round(recoveries.stream().mapToLong(Long::longValue).average().orElse(0));
    }
}
