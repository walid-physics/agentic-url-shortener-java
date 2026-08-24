package com.triviola.agentic.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateWorkflowRequest(
        @NotBlank @Size(max = 12_000) String requirement
) {}
