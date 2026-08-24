package com.triviola.agentic.api;

import com.triviola.agentic.orchestrator.WorkflowOrchestrator;
import com.triviola.agentic.orchestrator.WorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {
    private final WorkflowService workflowService;
    private final WorkflowOrchestrator orchestrator;
    private final WorkflowQueryService queryService;

    public WorkflowController(WorkflowService workflowService, WorkflowOrchestrator orchestrator,
                              WorkflowQueryService queryService) {
        this.workflowService = workflowService;
        this.orchestrator = orchestrator;
        this.queryService = queryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkflowResponse create(@Valid @RequestBody CreateWorkflowRequest request) {
        return WorkflowResponse.from(workflowService.create(request.requirement()));
    }

    @GetMapping("/{workflowId}")
    public WorkflowResponse get(@PathVariable UUID workflowId) {
        return WorkflowResponse.from(workflowService.get(workflowId));
    }

    @PostMapping("/{workflowId}/execute")
    public WorkflowResponse execute(@PathVariable UUID workflowId) {
        return WorkflowResponse.from(orchestrator.execute(workflowId));
    }

    @PostMapping("/{workflowId}/tasks/{taskKey}/approve")
    public WorkflowResponse approve(@PathVariable UUID workflowId, @PathVariable String taskKey) {
        workflowService.approve(workflowId, taskKey);
        return WorkflowResponse.from(orchestrator.execute(workflowId));
    }

    @PostMapping("/{workflowId}/tasks/{taskKey}/reject")
    public WorkflowResponse reject(@PathVariable UUID workflowId, @PathVariable String taskKey,
                                   @Valid @RequestBody RejectTaskRequest request) {
        return WorkflowResponse.from(workflowService.reject(workflowId, taskKey, request.reason()));
    }

    @GetMapping("/{workflowId}/events")
    public List<EventResponse> events(@PathVariable UUID workflowId) { return queryService.events(workflowId); }

    @GetMapping("/{workflowId}/decisions")
    public List<DecisionResponse> decisions(@PathVariable UUID workflowId) { return queryService.decisions(workflowId); }

    @GetMapping("/{workflowId}/metrics")
    public WorkflowMetricsResponse metrics(@PathVariable UUID workflowId) { return queryService.metrics(workflowId); }
}
