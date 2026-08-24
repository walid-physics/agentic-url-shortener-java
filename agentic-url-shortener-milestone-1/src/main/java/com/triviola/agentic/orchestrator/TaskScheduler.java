package com.triviola.agentic.orchestrator;

import com.triviola.agentic.workflow.TaskStatus;
import com.triviola.agentic.workflow.WorkflowEntity;
import com.triviola.agentic.workflow.WorkflowTaskEntity;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.List;

@Component
public class TaskScheduler {
    public List<WorkflowTaskEntity> findReadyTasks(WorkflowEntity workflow) {
        Map<String, WorkflowTaskEntity> byKey = workflow.getTasks().stream()
                .collect(Collectors.toMap(WorkflowTaskEntity::getTaskKey, Function.identity()));
        return workflow.getTasks().stream()
                .filter(task -> task.getStatus() == TaskStatus.PENDING)
                .filter(task -> task.getDependencies().stream()
                        .allMatch(key -> byKey.get(key).getStatus() == TaskStatus.PASSED))
                .toList();
    }
}
