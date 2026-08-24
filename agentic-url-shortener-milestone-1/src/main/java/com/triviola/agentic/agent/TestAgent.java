package com.triviola.agentic.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triviola.agentic.agent.output.*;
import com.triviola.agentic.config.AiProperties;
import com.triviola.agentic.orchestrator.AgentContext;
import com.triviola.agentic.workflow.AgentType;
import com.triviola.agentic.tools.BuildTool;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class TestAgent extends AbstractStructuredAgent<TestAnalysis> {
    private final BuildTool buildTool;

    public TestAgent(StructuredAiClient client, AiProperties properties, ObjectMapper mapper,
                     BuildTool buildTool) {
        super(client, properties, mapper, TestAnalysis.class);
        this.buildTool = buildTool;
    }
    @Override public AgentType supports() { return AgentType.TEST; }
    @Override protected String schemaName() { return "test_analysis"; }
    @Override protected String systemPrompt() { return """
        You are a skeptical test engineer. Evaluate the implementation artifact against the acceptance criteria.
        Report concrete coverage gaps and failure evidence. Mark successful false when release-blocking behavior
        is untested or contradicted. Do not claim that commands ran unless tool evidence is present.
        """; }
    @Override protected TestAnalysis demoOutput(AgentContext context) {
        return new TestAnalysis(true, "Deterministic demo validation passed",
                List.of("Workflow API integration path covered", "URL redirect edge cases specified"),
                List.of(), List.of(new AiDecision("Gate release on automated tests", "Review alone is insufficient evidence")));
    }

    @Override protected String userPrompt(AgentContext context) {
        String evidence = "No executable Maven Wrapper was present in the managed workspace.";
        if (buildTool.isAvailable()) {
            try {
                BuildTool.BuildResult result = buildTool.runTests();
                String output = result.output().length() > 12_000
                        ? result.output().substring(result.output().length() - 12_000)
                        : result.output();
                evidence = "exitCode=" + result.exitCode() + ", successful=" + result.successful() + "\n" + output;
            } catch (IOException | InterruptedException exception) {
                if (exception instanceof InterruptedException) Thread.currentThread().interrupt();
                evidence = "Test tool failed: " + exception.getMessage();
            }
        }
        return super.userPrompt(context) + "\n\nDeterministic Maven test evidence:\n" + evidence;
    }
}
