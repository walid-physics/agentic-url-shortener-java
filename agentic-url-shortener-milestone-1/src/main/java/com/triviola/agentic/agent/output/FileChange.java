package com.triviola.agentic.agent.output;

public record FileChange(String path, String operation, String content, String rationale) {}
