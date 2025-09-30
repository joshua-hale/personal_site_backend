package dev.joshuahale.backend.auth.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "sessions")
public class SessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK: sessions.user_id -> users.id (ON DELETE CASCADE in DB migration)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "session_token", nullable = false, unique = true, length = 255)
    private String sessionToken;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // === Lifecycle ===
    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    // === Convenience ===
    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }

    // === Getters / Setters ===
    public Long getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }
    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getSessionToken() {
        return sessionToken;
    }
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // === Equality: sessions are identified by token ===
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SessionEntity that)) return false;
        return Objects.equals(sessionToken, that.sessionToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionToken);
    }
}

