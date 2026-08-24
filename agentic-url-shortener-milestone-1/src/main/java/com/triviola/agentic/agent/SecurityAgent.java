package com.triviola.agentic.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triviola.agentic.agent.output.*;
import com.triviola.agentic.config.AiProperties;
import com.triviola.agentic.orchestrator.AgentContext;
import com.triviola.agentic.workflow.AgentType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SecurityAgent extends AbstractStructuredAgent<SecurityAnalysis> {
    public SecurityAgent(StructuredAiClient client, AiProperties properties, ObjectMapper mapper) {
        super(client, properties, mapper, SecurityAnalysis.class);
    }
    @Override public AgentType supports() { return AgentType.SECURITY; }
    @Override protected String schemaName() { return "security_analysis"; }
    @Override protected String systemPrompt() { return """
        You are an application security reviewer. Check URL-scheme validation, open-redirect abuse, SSRF
        considerations, rate limiting, short-code entropy, injection, analytics privacy, secrets, path traversal,
        and unsafe command execution. Mark successful false for unresolved high-severity findings.
        """; }
    @Override protected SecurityAnalysis demoOutput(AgentContext context) {
        return new SecurityAnalysis(true, "Security policy checks passed in demo mode",
                List.of("HTTP/HTTPS allowlist required", "Workspace path containment required"),
                List.of("Use SecureRandom codes", "Never execute model-generated shell text"),
                List.of(new AiDecision("Reject non-HTTP destination schemes", "Blocks javascript:, data:, and file: redirects")));
    }
}
