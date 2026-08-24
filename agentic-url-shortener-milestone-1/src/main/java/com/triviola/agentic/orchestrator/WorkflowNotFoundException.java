package com.triviola.agentic.orchestrator;

import java.util.UUID;

public class WorkflowNotFoundException extends RuntimeException {
    public WorkflowNotFoundException(UUID id) { super("Workflow not found: " + id); }
}
