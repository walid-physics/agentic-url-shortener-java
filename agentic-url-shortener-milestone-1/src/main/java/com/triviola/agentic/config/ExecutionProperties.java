package com.triviola.agentic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("agentic.execution")
public record ExecutionProperties(int parallelism, int taskTimeoutSeconds) {}
