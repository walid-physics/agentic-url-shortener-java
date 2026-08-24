package com.triviola.agentic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutionConfiguration {
    @Bean(destroyMethod = "shutdown")
    ExecutorService agentExecutor(ExecutionProperties properties) {
        return Executors.newFixedThreadPool(Math.max(1, properties.parallelism()));
    }
}
