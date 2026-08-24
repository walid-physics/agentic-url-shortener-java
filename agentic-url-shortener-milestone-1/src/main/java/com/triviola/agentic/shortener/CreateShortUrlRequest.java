package com.triviola.agentic.shortener;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CreateShortUrlRequest(@NotBlank @Size(max = 4_000) String originalUrl,
                                    Instant expiresAt) {}
