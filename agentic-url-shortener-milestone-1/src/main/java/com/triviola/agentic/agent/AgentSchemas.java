package com.triviola.agentic.agent;

import com.triviola.agentic.agent.output.*;

import java.util.Map;

final class AgentSchemas {
    private static final String DECISION_ITEMS = """
        {"type":"object","properties":{"decision":{"type":"string"},"rationale":{"type":"string"}},
         "required":["decision","rationale"],"additionalProperties":false}
        """;

    private static final Map<Class<?>, String> SCHEMAS = Map.of(
        RequirementAnalysis.class, object("""
            "summary":{"type":"string"},
            "ambiguities":{"type":"array","items":{"type":"string"}},
            "assumptions":{"type":"array","items":{"type":"string"}},
            "acceptanceCriteria":{"type":"array","items":{"type":"string"}},
            "risks":{"type":"array","items":{"type":"string"}},
            "decisions":{"type":"array","items":%s}
            """.formatted(DECISION_ITEMS), "summary,ambiguities,assumptions,acceptanceCriteria,risks,decisions"),
        ArchitectureAnalysis.class, object("""
            "summary":{"type":"string"},
            "affectedComponents":{"type":"array","items":{"type":"string"}},
            "architecturalChanges":{"type":"array","items":{"type":"string"}},
            "apiChanges":{"type":"array","items":{"type":"string"}},
            "dataModelChanges":{"type":"array","items":{"type":"string"}},
            "risks":{"type":"array","items":{"type":"string"}},
            "decisions":{"type":"array","items":%s}
            """.formatted(DECISION_ITEMS), "summary,affectedComponents,architecturalChanges,apiChanges,dataModelChanges,risks,decisions"),
        EngineeringPlan.class, object("""
            "summary":{"type":"string"},
            "tasks":{"type":"array","items":{"type":"object","properties":{
                "id":{"type":"string"},"description":{"type":"string"},"agent":{"type":"string"},
                "dependencies":{"type":"array","items":{"type":"string"}},"risk":{"type":"string"},
                "approvalRequired":{"type":"boolean"}},
                "required":["id","description","agent","dependencies","risk","approvalRequired"],
                "additionalProperties":false}},
            "risks":{"type":"array","items":{"type":"string"}},
            "decisions":{"type":"array","items":%s}
            """.formatted(DECISION_ITEMS), "summary,tasks,risks,decisions"),
        ImplementationProposal.class, object("""
            "summary":{"type":"string"},
            "fileChanges":{"type":"array","items":{"type":"object","properties":{
                "path":{"type":"string"},"operation":{"type":"string","enum":["UPSERT"]},
                "content":{"type":"string"},"rationale":{"type":"string"}},
                "required":["path","operation","content","rationale"],"additionalProperties":false}},
            "risks":{"type":"array","items":{"type":"string"}},
            "decisions":{"type":"array","items":%s}
            """.formatted(DECISION_ITEMS), "summary,fileChanges,risks,decisions"),
        TestAnalysis.class, validation("recommendedFixes"),
        SecurityAnalysis.class, validation("mitigations"),
        ReviewAnalysis.class, object("""
            "releaseReady":{"type":"boolean"},
            "summary":{"type":"string"},
            "blockers":{"type":"array","items":{"type":"string"}},
            "followUps":{"type":"array","items":{"type":"string"}},
            "decisions":{"type":"array","items":%s}
            """.formatted(DECISION_ITEMS), "releaseReady,summary,blockers,followUps,decisions")
    );

    private AgentSchemas() {}

    static String forType(Class<?> type) {
        String schema = SCHEMAS.get(type);
        if (schema == null) throw new IllegalArgumentException("No schema for " + type.getName());
        return schema;
    }

    private static String validation(String actionField) {
        return object("""
            "successful":{"type":"boolean"},
            "summary":{"type":"string"},
            "findings":{"type":"array","items":{"type":"string"}},
            "%s":{"type":"array","items":{"type":"string"}},
            "decisions":{"type":"array","items":%s}
            """.formatted(actionField, DECISION_ITEMS),
                "successful,summary,findings," + actionField + ",decisions");
    }

    private static String object(String properties, String requiredCsv) {
        String required = java.util.Arrays.stream(requiredCsv.split(","))
                .map(String::trim).map(value -> "\"" + value + "\"")
                .collect(java.util.stream.Collectors.joining(","));
        return "{\"type\":\"object\",\"properties\":{" + properties + "},\"required\":[" +
                required + "],\"additionalProperties\":false}";
    }
}
