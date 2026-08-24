package com.triviola.agentic.shortener;

import java.time.Instant;

public record UrlAnalyticsResponse(String code, long clickCount, Instant lastAccessedAt,
                                   Instant createdAt, Instant expiresAt, boolean active) {
    static UrlAnalyticsResponse from(ShortUrlEntity entity) {
        return new UrlAnalyticsResponse(entity.getCode(), entity.getClickCount(), entity.getLastAccessedAt(),
                entity.getCreatedAt(), entity.getExpiresAt(), entity.isActive());
    }
}
