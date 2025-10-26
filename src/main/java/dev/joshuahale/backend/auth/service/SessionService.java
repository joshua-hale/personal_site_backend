package dev.joshuahale.backend.auth.service;

import java.util.Optional;

public interface SessionService {

    /**
     * Create a new session for a user and return the opaque session token
     * (this is what you set as the cookie value).
     */
    String create(Long userId, String userAgent, String ipAddress);

    /**
     * Revoke a single session by its token. Returns true if a row was deleted.
     */
    boolean revoke(String sessionToken);

    /**
     * Revoke all sessions for a user.
     */
    void revokeAll(Long userId);

    /**
     * Validate a session token. Returns the associated userId if the token exists and is not expired.
     */
    Optional<Long> validate(String sessionToken);

    /**
     * Delete all sessions whose expiresAt is in the past. Returns count deleted.
     */
    long purgeExpired();
}
