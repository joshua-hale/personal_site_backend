package dev.joshuahale.backend.auth.repository;

import dev.joshuahale.backend.auth.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<SessionEntity, Long> {

    Optional<SessionEntity> findBySessionToken(String sessionToken);

    boolean existsBySessionToken(String sessionToken);

    // Housekeeping
    @Transactional
    long deleteByExpiresAtBefore(OffsetDateTime cutoff);

    // When logging a user out everywhere
    @Transactional
    long deleteByUser_Id(Long userId);
}

