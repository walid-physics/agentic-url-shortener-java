package com.triviola.agentic.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triviola.agentic.agent.output.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AgentSchemasTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void everyAgentSchemaIsValidJsonAndClosesAdditionalProperties() throws Exception {
        List<Class<?>> types = List.of(RequirementAnalysis.class, ArchitectureAnalysis.class,
                EngineeringPlan.class, ImplementationProposal.class, TestAnalysis.class,
                SecurityAnalysis.class, ReviewAnalysis.class);

        for (Class<?> type : types) {
            var schema = objectMapper.readTree(AgentSchemas.forType(type));
            assertEquals("object", schema.path("type").asText());
            assertFalse(schema.path("additionalProperties").asBoolean(true));
        }
    }
}
