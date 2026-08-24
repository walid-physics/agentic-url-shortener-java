package com.triviola.agentic.shortener;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "short_urls")
public class ShortUrlEntity {
    @Id private UUID id;
    @Column(nullable = false, unique = true, length = 16) private String code;
    @Column(nullable = false, length = 4_000) private String originalUrl;
    @Column(nullable = false) private Instant createdAt;
    private Instant expiresAt;
    @Column(nullable = false) private long clickCount;
    private Instant lastAccessedAt;
    @Column(nullable = false) private boolean active;
    @Version private long version;

    protected ShortUrlEntity() {}

    public ShortUrlEntity(UUID id, String code, String originalUrl, Instant expiresAt) {
        this.id = id;
        this.code = code;
        this.originalUrl = originalUrl;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
        this.active = true;
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getOriginalUrl() { return originalUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public long getClickCount() { return clickCount; }
    public Instant getLastAccessedAt() { return lastAccessedAt; }
    public boolean isActive() { return active; }
    public long getVersion() { return version; }

    public boolean isExpired(Instant now) { return expiresAt != null && !expiresAt.isAfter(now); }
    public void recordClick(Instant now) { clickCount++; lastAccessedAt = now; }
    public void deactivate() { active = false; }
}
