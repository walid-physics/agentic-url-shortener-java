package com.triviola.agentic.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triviola.agentic.agent.output.*;
import com.triviola.agentic.config.AiProperties;
import com.triviola.agentic.orchestrator.AgentContext;
import com.triviola.agentic.workflow.AgentType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlannerAgent extends AbstractStructuredAgent<EngineeringPlan> {
    public PlannerAgent(StructuredAiClient client, AiProperties properties, ObjectMapper mapper) {
        super(client, properties, mapper, EngineeringPlan.class);
    }
    @Override public AgentType supports() { return AgentType.PLANNER; }
    @Override protected String schemaName() { return "engineering_plan"; }
    @Override protected String systemPrompt() { return """
        You are a technical planner. Produce small actionable tasks with explicit dependencies, risk,
        and approval requirements. Tests and security review should run in parallel after implementation.
        You propose the plan; the Java orchestrator remains the execution authority.
        """; }
    @Override protected EngineeringPlan demoOutput(AgentContext context) {
        return new EngineeringPlan("Dependency-aware implementation and validation plan", List.of(
                new PlannedTask("implementation", "Implement bounded change", "IMPLEMENTATION", List.of(), "HIGH", true),
                new PlannedTask("tests", "Run validation", "TEST", List.of("implementation"), "MEDIUM", false),
                new PlannedTask("security", "Review security", "SECURITY", List.of("implementation"), "MEDIUM", false)),
                List.of("Migration compatibility", "regression risk"),
                List.of(new AiDecision("Parallelize tests and security", "They are independent after implementation")));
    }
}
