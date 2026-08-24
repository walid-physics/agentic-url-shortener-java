package com.triviola.agentic.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triviola.agentic.agent.output.*;
import com.triviola.agentic.config.AiProperties;
import com.triviola.agentic.orchestrator.AgentContext;
import com.triviola.agentic.workflow.AgentType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReviewerAgent extends AbstractStructuredAgent<ReviewAnalysis> {
    public ReviewerAgent(StructuredAiClient client, AiProperties properties, ObjectMapper mapper) {
        super(client, properties, mapper, ReviewAnalysis.class);
    }
    @Override public AgentType supports() { return AgentType.REVIEWER; }
    @Override protected String schemaName() { return "review_analysis"; }
    @Override protected String systemPrompt() { return """
        You are the release reviewer. Reconcile requirements, plan, implementation, test, and security artifacts.
        Release is ready only when acceptance criteria are satisfied and there are no unresolved blockers.
        Provide an auditable summary; do not override orchestrator policy.
        """; }
    @Override protected ReviewAnalysis demoOutput(AgentContext context) {
        return new ReviewAnalysis(true, "All governed demo gates passed", List.of(),
                List.of("Run the same workflow with AI enabled for model-generated artifacts"),
                List.of(new AiDecision("Mark the workflow release-ready", "Test and security branches both passed")));
    }
}
