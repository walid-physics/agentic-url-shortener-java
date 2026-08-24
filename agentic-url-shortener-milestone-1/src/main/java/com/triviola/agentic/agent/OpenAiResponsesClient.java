package com.triviola.agentic.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.triviola.agentic.config.AiProperties;
import org.springframework.stereotype.Component;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Component
public class OpenAiResponsesClient implements StructuredAiClient {
    private final AiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public OpenAiResponsesClient(AiProperties properties, ObjectMapper objectMapper,
                                 RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(Math.max(1, properties.requestTimeoutSeconds())));
        this.restClient = restClientBuilder.baseUrl(properties.baseUrl())
                .requestFactory(requestFactory).build();
    }

    @Override
    public <T> T generate(String schemaName, String jsonSchema, String systemPrompt,
                          String userPrompt, Class<T> outputType) {
        if (!properties.enabled()) {
            throw new IllegalStateException("AI execution is disabled");
        }
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY is required when AGENTIC_AI_ENABLED=true");
        }

        ObjectNode request = objectMapper.createObjectNode();
        request.put("model", properties.model());
        request.put("max_output_tokens", properties.maxOutputTokens());
        request.putObject("reasoning").put("effort", properties.reasoningEffort());
        ArrayNode input = request.putArray("input");
        input.add(message("system", systemPrompt));
        input.add(message("user", userPrompt));

        ObjectNode format = request.putObject("text").putObject("format");
        format.put("type", "json_schema");
        format.put("name", schemaName);
        format.put("strict", true);
        try {
            format.set("schema", objectMapper.readTree(jsonSchema));
            JsonNode response = restClient.post()
                    .uri("/v1/responses")
                    .header("Authorization", "Bearer " + properties.apiKey())
                    .header("Content-Type", "application/json")
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);
            String outputText = extractOutputText(response);
            return objectMapper.readValue(outputText, outputType);
        } catch (Exception exception) {
            throw new IllegalStateException("Structured AI response failed (" +
                    exception.getClass().getSimpleName() + ")", exception);
        }
    }

    private ObjectNode message(String role, String content) {
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private String extractOutputText(JsonNode response) {
        if (response == null) throw new IllegalStateException("OpenAI returned an empty response");
        for (JsonNode output : response.path("output")) {
            for (JsonNode content : output.path("content")) {
                if ("refusal".equals(content.path("type").asText())) {
                    throw new IllegalStateException("Model refused: " + content.path("refusal").asText());
                }
                if ("output_text".equals(content.path("type").asText()) && content.has("text")) {
                    return content.path("text").asText();
                }
            }
        }
        throw new IllegalStateException("Response contained no output_text item");
    }
}
