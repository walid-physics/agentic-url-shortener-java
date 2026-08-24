package com.triviola.agentic.orchestrator;

import com.triviola.agentic.workflow.AgentType;

public interface EngineeringAgent {
    AgentType supports();
    AgentExecutionResult execute(AgentContext context);
}
