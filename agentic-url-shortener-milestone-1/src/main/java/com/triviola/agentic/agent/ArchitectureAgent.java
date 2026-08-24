package com.triviola.agentic.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triviola.agentic.agent.output.*;
import com.triviola.agentic.config.AiProperties;
import com.triviola.agentic.orchestrator.AgentContext;
import com.triviola.agentic.workflow.AgentType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ArchitectureAgent extends AbstractStructuredAgent<ArchitectureAnalysis> {
    public ArchitectureAgent(StructuredAiClient client, AiProperties properties, ObjectMapper mapper) {
        super(client, properties, mapper, ArchitectureAnalysis.class);
    }
    @Override public AgentType supports() { return AgentType.ARCHITECTURE; }
    @Override protected String schemaName() { return "architecture_analysis"; }
    @Override protected String systemPrompt() { return """
        You are a senior Java/Spring architect. Analyze affected components, APIs, persistence, boundaries,
        and risks. Preserve separation between the governed orchestrator and the URL-shortener product.
        Do not invent files that are not justified by the supplied artifacts.
        """; }
    @Override protected ArchitectureAnalysis demoOutput(AgentContext context) {
        return new ArchitectureAnalysis("Layered Spring Boot architecture with a persistent orchestration control plane",
                List.of("workflow", "orchestrator", "agent", "shortener", "api"),
                List.of("DAG scheduler owns execution", "Agents return typed proposals"),
                List.of("Workflow lifecycle REST API", "URL creation, redirect, analytics, deletion APIs"),
                List.of("Workflow events", "decision records", "short URL analytics"),
                List.of("Concurrent state updates", "untrusted model output"),
                List.of(new AiDecision("Keep agents behind an interface", "Allows deterministic testing and provider replacement")));
    }
}
