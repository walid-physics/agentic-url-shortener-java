package com.triviola.agentic.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triviola.agentic.agent.output.*;
import com.triviola.agentic.config.AiProperties;
import com.triviola.agentic.orchestrator.AgentContext;
import com.triviola.agentic.workflow.AgentType;
import com.triviola.agentic.tools.SafeWorkspaceTool;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class ImplementationAgent extends AbstractStructuredAgent<ImplementationProposal> {
    private final SafeWorkspaceTool workspaceTool;

    public ImplementationAgent(StructuredAiClient client, AiProperties properties, ObjectMapper mapper,
                               SafeWorkspaceTool workspaceTool) {
        super(client, properties, mapper, ImplementationProposal.class);
        this.workspaceTool = workspaceTool;
    }
    @Override public AgentType supports() { return AgentType.IMPLEMENTATION; }
    @Override protected String schemaName() { return "implementation_proposal"; }
    @Override protected String systemPrompt() { return """
        You are a senior Java 21 and Spring Boot implementation agent. Return the smallest reviewable set of
        complete file upserts needed by the approved plan. Paths must be relative, must not contain '..', and
        must stay inside the managed product workspace. Never propose secrets, shell commands, deletions,
        deployment, or changes to the orchestrator's governance code.
        """; }
    @Override protected ImplementationProposal demoOutput(AgentContext context) {
        return new ImplementationProposal("Demo mode validated the bounded implementation stage",
                List.of(), List.of("Demo mode does not mutate source files"),
                List.of(new AiDecision("Require human approval before implementation", "Implementation is classified high risk")));
    }

    @Override protected void afterGenerate(ImplementationProposal output, AgentContext context) {
        if (output.fileChanges().isEmpty()) return;
        try {
            workspaceTool.applyUpserts(output.fileChanges());
        } catch (IOException | SecurityException exception) {
            throw new IllegalStateException("Controlled workspace update failed: " + exception.getMessage(), exception);
        }
    }
}
