package com.triviola.agentic.shortener;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ShortUrlRepository extends JpaRepository<ShortUrlEntity, UUID> {
    Optional<ShortUrlEntity> findByCode(String code);
    boolean existsByCode(String code);
}
