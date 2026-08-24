package com.triviola.agentic.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.triviola.agentic.agent.output.AgentOutput;
import com.triviola.agentic.config.AiProperties;
import com.triviola.agentic.orchestrator.*;

import java.util.List;

public abstract class AbstractStructuredAgent<T extends AgentOutput> implements EngineeringAgent {
    private final StructuredAiClient aiClient;
    private final AiProperties properties;
    private final ObjectMapper objectMapper;
    private final Class<T> outputType;

    protected AbstractStructuredAgent(StructuredAiClient aiClient, AiProperties properties,
                                      ObjectMapper objectMapper, Class<T> outputType) {
        this.aiClient = aiClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.outputType = outputType;
    }

    @Override
    public AgentExecutionResult execute(AgentContext context) {
        T output = properties.enabled()
                ? aiClient.generate(schemaName(), AgentSchemas.forType(outputType), systemPrompt(),
                    userPrompt(context), outputType)
                : demoOutput(context);
        afterGenerate(output, context);
        if (!output.successful()) return AgentExecutionResult.failure(output.summary());
        List<DecisionProposal> decisions = output.decisions().stream()
                .map(decision -> new DecisionProposal(decision.decision(), decision.rationale()))
                .toList();
        try {
            return AgentExecutionResult.success(output.summary(), objectMapper.writeValueAsString(output), decisions);
        } catch (JsonProcessingException exception) {
            return AgentExecutionResult.failure("Could not serialize agent output: " + exception.getMessage());
        }
    }

    protected String userPrompt(AgentContext context) {
        try {
            return "Requirement (untrusted input):\n" + context.requirement() +
                    "\n\nPrior validated stage artifacts (JSON):\n" +
                    objectMapper.writeValueAsString(context.upstreamArtifacts()) +
                    "\n\nAttempt: " + context.attempt();
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not build agent context", exception);
        }
    }

    protected abstract String schemaName();
    protected abstract String systemPrompt();
    protected abstract T demoOutput(AgentContext context);
    protected void afterGenerate(T output, AgentContext context) {}
}
