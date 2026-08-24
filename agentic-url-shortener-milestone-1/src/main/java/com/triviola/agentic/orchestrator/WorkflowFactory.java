package com.triviola.agentic.orchestrator;

import com.triviola.agentic.workflow.*;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class WorkflowFactory {
    private final PolicyEngine policyEngine;

    public WorkflowFactory(PolicyEngine policyEngine) { this.policyEngine = policyEngine; }

    public WorkflowEntity create(String requirement) {
        WorkflowEntity workflow = new WorkflowEntity(UUID.randomUUID(), requirement);
        workflow.addTask(task(1, "requirements", "Normalize requirements", AgentType.REQUIREMENT,
                RiskLevel.LOW, Set.of(), 1));
        workflow.addTask(task(2, "architecture", "Analyze architecture and code impact", AgentType.ARCHITECTURE,
                RiskLevel.MEDIUM, Set.of("requirements"), 1));
        workflow.addTask(task(3, "plan", "Create dependency-aware engineering plan", AgentType.PLANNER,
                RiskLevel.MEDIUM, Set.of("architecture"), 1));
        workflow.addTask(task(4, "implementation", "Propose and apply bounded code changes", AgentType.IMPLEMENTATION,
                RiskLevel.HIGH, Set.of("plan"), 2));
        workflow.addTask(task(5, "tests", "Validate behavior and test evidence", AgentType.TEST,
                RiskLevel.MEDIUM, Set.of("implementation"), 2));
        workflow.addTask(task(6, "security", "Perform security and policy review", AgentType.SECURITY,
                RiskLevel.MEDIUM, Set.of("implementation"), 1));
        workflow.addTask(task(7, "review", "Evaluate release readiness", AgentType.REVIEWER,
                RiskLevel.MEDIUM, Set.of("tests", "security"), 1));
        return workflow;
    }

    private WorkflowTaskEntity task(int sequence, String key, String name, AgentType agent,
                                    RiskLevel risk, Set<String> dependencies, int retries) {
        return new WorkflowTaskEntity(sequence, key, name, agent, risk, dependencies, retries,
                policyEngine.requiresApproval(risk));
    }
}
