package com.triviola.agentic.agent;

public interface StructuredAiClient {
    <T> T generate(String schemaName, String jsonSchema, String systemPrompt,
                   String userPrompt, Class<T> outputType);
}
