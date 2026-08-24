package com.triviola.agentic.orchestrator;

import com.triviola.agentic.workflow.AgentType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class AgentRegistry {
    private final Map<AgentType, EngineeringAgent> agents = new EnumMap<>(AgentType.class);

    public AgentRegistry(List<EngineeringAgent> engineeringAgents) {
        engineeringAgents.forEach(agent -> {
            if (agents.put(agent.supports(), agent) != null) {
                throw new IllegalStateException("Duplicate agent for " + agent.supports());
            }
        });
    }

    public AgentExecutionResult execute(AgentContext context) {
        EngineeringAgent agent = agents.get(context.agentType());
        if (agent == null) throw new IllegalStateException("No agent registered for " + context.agentType());
        return agent.execute(context);
    }
}
