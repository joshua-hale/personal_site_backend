package dev.joshuahale.backend.auth.service.impl;

import dev.joshuahale.backend.auth.entity.SessionEntity;
import dev.joshuahale.backend.auth.entity.UserEntity;
import dev.joshuahale.backend.auth.repository.SessionRepository;
import dev.joshuahale.backend.auth.repository.UserRepository;
import dev.joshuahale.backend.auth.service.SessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@Transactional
public class SessionServiceImpl implements SessionService {

    // === config ===
    private static final SecureRandom RNG = new SecureRandom();
    private static final Base64.Encoder B64URL = Base64.getUrlEncoder().withoutPadding();
    private static final int TOKEN_BYTES = 32; // 256-bit; base64url ~43 chars (fits length=255 easily)
    private final Duration sessionTtl = Duration.ofDays(7);

    // === deps ===
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public SessionServiceImpl(SessionRepository sessionRepository,
                              UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public String create(Long userId, String userAgent, String ipAddress) {
        // Load a reference to the user
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Generate a unique token (retry if ultra-rare collision)
        String token = generateUniqueToken();

        SessionEntity s = new SessionEntity();
        s.setUser(user);
        s.setSessionToken(token);
        s.setExpiresAt(OffsetDateTime.now().plus(sessionTtl));
        // your entity has only createdAt via @PrePersist; no updatedAt field -> nothing else to set

        sessionRepository.save(s);
        return token;
    }

    @Override
    public boolean revoke(String sessionToken) {
        // delete by token (since you model revocation as deletion)
        return sessionRepository.findBySessionToken(sessionToken)
                .map(se -> { sessionRepository.delete(se); return true; })
                .orElse(false);
    }

    @Override
    public void revokeAll(Long userId) {
        sessionRepository.deleteByUser_Id(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> validate(String sessionToken) {
        if (sessionToken == null || sessionToken.isBlank()) return Optional.empty();

        return sessionRepository.findBySessionToken(sessionToken)
                .filter(se -> se.getExpiresAt() != null && se.getExpiresAt().isAfter(OffsetDateTime.now()))
                .map(se -> se.getUser().getId());
    }

    @Override
    public long purgeExpired() {
        return sessionRepository.deleteByExpiresAtBefore(OffsetDateTime.now());
    }

    // === helpers ===

    private String generateUniqueToken() {
        // Rare collisions are possible in theory; loop just in case
        for (int i = 0; i < 5; i++) {
            String t = randomToken();
            if (!sessionRepository.existsBySessionToken(t)) return t;
        }
        // If we somehow collided multiple times, just generate without checking; DB unique constraint will enforce.
        return randomToken();
    }

    private String randomToken() {
        byte[] buf = new byte[TOKEN_BYTES];
        RNG.nextBytes(buf);
        return B64URL.encodeToString(buf);
    }
}
