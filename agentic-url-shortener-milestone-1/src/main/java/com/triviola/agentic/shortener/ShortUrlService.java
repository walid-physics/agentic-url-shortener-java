package com.triviola.agentic.shortener;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class ShortUrlService {
    private static final char[] ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final int CODE_LENGTH = 8;
    private static final int MAX_COLLISION_ATTEMPTS = 10;
    private final SecureRandom secureRandom = new SecureRandom();
    private final ShortUrlRepository repository;

    public ShortUrlService(ShortUrlRepository repository) { this.repository = repository; }

    @Transactional
    public ShortUrlEntity create(CreateShortUrlRequest request) {
        String normalized = validateDestination(request.originalUrl());
        if (request.expiresAt() != null && !request.expiresAt().isAfter(Instant.now())) {
            throw new IllegalArgumentException("expiresAt must be in the future");
        }
        return repository.save(new ShortUrlEntity(UUID.randomUUID(), generateUniqueCode(),
                normalized, request.expiresAt()));
    }

    @Transactional
    public ShortUrlEntity resolve(String code) {
        ShortUrlEntity entity = get(code);
        if (!entity.isActive()) throw new ShortUrlNotFoundException(code);
        Instant now = Instant.now();
        if (entity.isExpired(now)) throw new ShortUrlExpiredException(code);
        entity.recordClick(now);
        return entity;
    }

    @Transactional(readOnly = true)
    public ShortUrlEntity get(String code) {
        validateCode(code);
        return repository.findByCode(code).orElseThrow(() -> new ShortUrlNotFoundException(code));
    }

    @Transactional
    public void delete(String code) {
        ShortUrlEntity entity = get(code);
        entity.deactivate();
    }

    private String validateDestination(String rawUrl) {
        try {
            URI uri = new URI(rawUrl.trim());
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!(scheme.equals("http") || scheme.equals("https"))) {
                throw new IllegalArgumentException("Only HTTP and HTTPS destination URLs are permitted");
            }
            if (uri.getHost() == null || uri.getHost().isBlank() || uri.getUserInfo() != null) {
                throw new IllegalArgumentException("Destination URL must have a valid host and no user-info component");
            }
            return uri.normalize().toASCIIString();
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("Destination URL is malformed");
        }
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < MAX_COLLISION_ATTEMPTS; attempt++) {
            char[] code = new char[CODE_LENGTH];
            for (int i = 0; i < code.length; i++) code[i] = ALPHABET[secureRandom.nextInt(ALPHABET.length)];
            String candidate = new String(code);
            if (!repository.existsByCode(candidate)) return candidate;
        }
        throw new IllegalStateException("Could not allocate a unique short code");
    }

    private void validateCode(String code) {
        if (code == null || !code.matches("[A-Za-z0-9]{8}")) {
            throw new IllegalArgumentException("Short code must contain exactly 8 ASCII letters or digits");
        }
    }
}
