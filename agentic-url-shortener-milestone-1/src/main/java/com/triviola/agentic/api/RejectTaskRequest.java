package com.triviola.agentic.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectTaskRequest(@NotBlank @Size(max = 2_000) String reason) {}
