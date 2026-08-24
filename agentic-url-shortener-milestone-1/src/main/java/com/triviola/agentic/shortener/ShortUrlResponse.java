package com.triviola.agentic.shortener;

import java.time.Instant;
import java.util.UUID;

public record ShortUrlResponse(UUID id, String code, String shortPath, String originalUrl,
                               Instant createdAt, Instant expiresAt, boolean active) {
    static ShortUrlResponse from(ShortUrlEntity entity) {
        return new ShortUrlResponse(entity.getId(), entity.getCode(), "/r/" + entity.getCode(),
                entity.getOriginalUrl(), entity.getCreatedAt(), entity.getExpiresAt(), entity.isActive());
    }
}
