package com.triviola.agentic.orchestrator;

import com.triviola.agentic.config.ExecutionProperties;
import com.triviola.agentic.workflow.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class WorkflowOrchestrator {
    private static final int MAX_SCHEDULER_ITERATIONS = 100;
    private final WorkflowRepository workflowRepository;
    private final TaskScheduler scheduler;
    private final AgentRegistry agentRegistry;
    private final AuditService auditService;
    private final DecisionService decisionService;
    private final ExecutorService executor;
    private final ExecutionProperties executionProperties;

    public WorkflowOrchestrator(WorkflowRepository workflowRepository, TaskScheduler scheduler,
                                AgentRegistry agentRegistry, AuditService auditService,
                                DecisionService decisionService, ExecutorService executor,
                                ExecutionProperties executionProperties) {
        this.workflowRepository = workflowRepository;
        this.scheduler = scheduler;
        this.agentRegistry = agentRegistry;
        this.auditService = auditService;
        this.decisionService = decisionService;
        this.executor = executor;
        this.executionProperties = executionProperties;
    }

    public WorkflowEntity execute(UUID workflowId) {
        WorkflowEntity workflow = get(workflowId);
        if (isTerminal(workflow.getStatus())) return workflow;
        if (workflow.getStatus() == WorkflowStatus.WAITING_APPROVAL) return workflow;

        boolean firstStart = workflow.getStartedAt() == null;
        workflow.start();
        workflow = workflowRepository.save(workflow);
        if (firstStart) {
            auditService.record(workflowId, null, EventType.WORKFLOW_STARTED, 0, 0,
                    workflow.getRequirement(), "running", "Workflow execution started");
        }

        for (int iteration = 0; iteration < MAX_SCHEDULER_ITERATIONS; iteration++) {
            if (allPassed(workflow)) return complete(workflow);

            List<WorkflowTaskEntity> ready = scheduler.findReadyTasks(workflow);
            List<String> executableKeys = new ArrayList<>();
            for (WorkflowTaskEntity task : ready) {
                if (task.isApprovalRequired() && !task.isApproved()) {
                    task.waitForApproval();
                    auditService.record(workflowId, task.getTaskKey(), EventType.APPROVAL_REQUIRED,
                            task.getRetryCount() + 1, 0, task.getRiskLevel().name(), "waiting",
                            "Policy requires human approval");
                } else {
                    executableKeys.add(task.getTaskKey());
                }
            }
            workflow = workflowRepository.save(workflow);

            if (executableKeys.isEmpty()) {
                if (hasWaitingApproval(workflow)) {
                    workflow.waitForApproval();
                    return workflowRepository.save(workflow);
                }
                return safeStop(workflow, "No runnable tasks remain");
            }

            Map<String, String> artifacts = passedArtifacts(workflow);
            List<TaskInvocation> invocations = new ArrayList<>();
            for (String executableKey : executableKeys) {
                WorkflowTaskEntity task = findTask(workflow, executableKey);
                task.markRunning();
                AgentContext context = new AgentContext(workflowId, task.getTaskKey(), task.getAgentType(),
                        workflow.getRequirement(), artifacts, task.getRetryCount() + 1);
                auditService.record(workflowId, task.getTaskKey(), EventType.TASK_STARTED,
                        context.attempt(), 0, context.toString(), "running", task.getName());
                invocations.add(new TaskInvocation(task.getTaskKey(), context, Instant.now()));
            }
            workflow = workflowRepository.save(workflow);

            List<Future<TaskOutcome>> futures = invocations.stream()
                    .map(invocation -> executor.submit(() -> run(invocation)))
                    .toList();
            for (int i = 0; i < futures.size(); i++) {
                Future<TaskOutcome> future = futures.get(i);
                TaskInvocation invocation = invocations.get(i);
                long deadlineMs = invocation.startedAt().toEpochMilli() +
                        TimeUnit.SECONDS.toMillis(executionProperties.taskTimeoutSeconds());
                long remainingMs = Math.max(1, deadlineMs - Instant.now().toEpochMilli());
                try {
                    apply(workflow, future.get(remainingMs, TimeUnit.MILLISECONDS));
                } catch (TimeoutException timeout) {
                    future.cancel(true);
                    apply(workflow, failed(invocation, timeout));
                } catch (ExecutionException failure) {
                    apply(workflow, failed(invocation, failure));
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    future.cancel(true);
                    apply(workflow, failed(invocation, interrupted));
                }
            }
            workflow = workflowRepository.save(workflow);
        }

        return safeStop(workflow, "Scheduler iteration limit exceeded");
    }

    private TaskOutcome run(TaskInvocation invocation) {
        AgentExecutionResult result = agentRegistry.execute(invocation.context());
        long duration = Duration.between(invocation.startedAt(), Instant.now()).toMillis();
        return new TaskOutcome(invocation.taskKey(), invocation.context(), result, duration);
    }

    private TaskOutcome failed(TaskInvocation invocation, Throwable error) {
        Throwable cause = (error instanceof CompletionException || error instanceof ExecutionException)
                && error.getCause() != null ? error.getCause() : error;
        long duration = Duration.between(invocation.startedAt(), Instant.now()).toMillis();
        return new TaskOutcome(invocation.taskKey(), invocation.context(),
                AgentExecutionResult.failure(cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage()),
                duration);
    }

    private void apply(WorkflowEntity workflow, TaskOutcome outcome) {
        WorkflowTaskEntity task = findTask(workflow, outcome.taskKey());
        AgentExecutionResult result = outcome.result();
        if (result.successful()) {
            task.pass(result.outputArtifact());
            decisionService.recordAll(workflow.getId(), task.getTaskKey(), task.getAgentType(), result.decisions());
            auditService.record(workflow.getId(), task.getTaskKey(), EventType.TASK_PASSED,
                    outcome.context().attempt(), outcome.durationMs(), outcome.context().toString(),
                    result.outputArtifact(), result.summary());
            return;
        }

        auditService.record(workflow.getId(), task.getTaskKey(), EventType.TASK_FAILED,
                outcome.context().attempt(), outcome.durationMs(), outcome.context().toString(),
                result.summary(), result.summary());
        boolean retryScheduled = task.retry(result.summary());
        if (retryScheduled) {
            auditService.record(workflow.getId(), task.getTaskKey(), EventType.RETRY_SCHEDULED,
                    task.getRetryCount() + 1, 0, result.summary(), "pending",
                    "Bounded retry scheduled");
        }
    }

    private WorkflowEntity complete(WorkflowEntity workflow) {
        workflow.complete();
        WorkflowEntity saved = workflowRepository.save(workflow);
        auditService.record(workflow.getId(), null, EventType.WORKFLOW_COMPLETED, 0,
                Duration.between(workflow.getStartedAt(), workflow.getCompletedAt()).toMillis(),
                workflow.getRequirement(), "completed", "Release gate passed");
        return saved;
    }

    private WorkflowEntity safeStop(WorkflowEntity workflow, String reason) {
        workflow.getTasks().stream().filter(task -> task.getStatus() == TaskStatus.PENDING)
                .forEach(WorkflowTaskEntity::block);
        workflow.safeStop();
        WorkflowEntity saved = workflowRepository.save(workflow);
        auditService.record(workflow.getId(), null, EventType.SAFE_STOPPED, 0, 0,
                workflow.getRequirement(), "safe_stopped", reason);
        return saved;
    }

    private WorkflowEntity get(UUID id) {
        return workflowRepository.findById(id).orElseThrow(() -> new WorkflowNotFoundException(id));
    }

    private boolean allPassed(WorkflowEntity workflow) {
        return workflow.getTasks().stream().allMatch(task -> task.getStatus() == TaskStatus.PASSED);
    }

    private boolean hasWaitingApproval(WorkflowEntity workflow) {
        return workflow.getTasks().stream().anyMatch(task -> task.getStatus() == TaskStatus.WAITING_APPROVAL);
    }

    private boolean isTerminal(WorkflowStatus status) {
        return status == WorkflowStatus.COMPLETED || status == WorkflowStatus.REJECTED ||
                status == WorkflowStatus.SAFE_STOPPED;
    }

    private WorkflowTaskEntity findTask(WorkflowEntity workflow, String taskKey) {
        return workflow.getTasks().stream()
                .filter(task -> task.getTaskKey().equals(taskKey))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Task disappeared from workflow: " + taskKey));
    }

    private Map<String, String> passedArtifacts(WorkflowEntity workflow) {
        return workflow.getTasks().stream()
                .filter(task -> task.getStatus() == TaskStatus.PASSED)
                .collect(Collectors.toMap(WorkflowTaskEntity::getTaskKey,
                        task -> Objects.requireNonNullElse(task.getOutputArtifact(), ""),
                        (left, right) -> right, LinkedHashMap::new));
    }

    private record TaskInvocation(String taskKey, AgentContext context, Instant startedAt) {}
    private record TaskOutcome(String taskKey, AgentContext context,
                               AgentExecutionResult result, long durationMs) {}
}
