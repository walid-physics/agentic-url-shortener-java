package com.triviola.agentic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("agentic.ai")
public record AiProperties(boolean enabled, String apiKey, String baseUrl, String model,
                           int maxOutputTokens, String reasoningEffort, int requestTimeoutSeconds) {}
