package dev.joshuahale.backend.auth.service.impl;

import dev.joshuahale.backend.auth.dto.AuthResponse;
import dev.joshuahale.backend.auth.dto.LoginRequest;
import dev.joshuahale.backend.auth.dto.SignupRequest;
import dev.joshuahale.backend.auth.entity.UserEntity;
import dev.joshuahale.backend.auth.repository.UserRepository;
import dev.joshuahale.backend.auth.service.AuthService;
import dev.joshuahale.backend.auth.service.PasswordService;
import dev.joshuahale.backend.auth.service.SessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final PasswordService passwordService;

    public AuthServiceImpl(UserRepository userRepository,
                           SessionService sessionService,
                           PasswordService passwordService) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
        this.passwordService = passwordService;
    }

    // =========================
    // Registration
    // =========================
    @Override
    public AuthResponse register(SignupRequest request) {
        final String emailNorm = normalizeEmail(request.getEmail());
        final String usernameRaw = safeTrim(request.getUsername());

        // Uniqueness (also enforce with DB unique indexes)
        if (userRepository.existsByEmail(emailNorm)) {
            throw new DuplicateEmailException("Email already in use");
        }
        if (userRepository.existsByUsername(usernameRaw)) {
            throw new DuplicateUsernameException("Username already in use");
        }

        // Persist user
        UserEntity user = new UserEntity();
        user.setEmail(emailNorm);
        user.setUsername(usernameRaw);
        user.setPasswordHash(passwordService.hash(request.getPassword()));
        user.setActive(true); // Personal site: active immediately

        UserEntity saved = userRepository.save(user);

        // New users may not have roles linked yet; return empty set if so
        return toAuthResponse(saved);
    }

    // =========================
    // Login
    // =========================
    @Override
    public AuthResponse login(LoginRequest request, String userAgent, String ipAddress) {
        final String login = safeTrim(request.getEmailOrUsername());

        UserEntity user = userRepository.findByEmailOrUsername(login)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!user.isActive()) {
            throw new AccountInactiveException("Account is inactive");
        }

        if (!passwordService.verify(request.getPassword(), user.getPasswordHash())) {
            // (Optional) record throttle here
            throw new InvalidCredentialsException("Invalid credentials");
        }
        // (Optional) clear throttle here

        // Book-keeping
        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);

        // Create a server-side session; controller will set the cookie
        sessionService.create(user.getId(), userAgent, ipAddress);

        // For RBAC-aware UI, return roles if available
        // (If you want roles eagerly here, you can add findByEmailOrUsernameWithRoles and use it.)
        return toAuthResponse(user);
    }

    // =========================
    // Logout(s)
    // =========================
    @Override
    public void logout(String sessionId) {
        boolean revoked = sessionService.revoke(sessionId);
        if (!revoked) {
            // Not fatal; you can silently ignore if you prefer
            throw new SessionNotFoundException("Session not found");
        }
    }

    @Override
    public void logoutAll(Long userId) {
        sessionService.revokeAll(userId);
    }

    // =========================
    // Current user from session
    // =========================
    @Override
    @Transactional(readOnly = true)
    public Optional<AuthResponse> currentUser(String sessionId) {
        var userIdOpt = sessionService.validate(sessionId);
        if (userIdOpt.isEmpty()) return Optional.empty();

        // Fetch with roles to populate AuthResponse accurately
        return userRepository.findByIdWithRoles(userIdOpt.get())
                .map(this::toAuthResponse);
    }

    // =========================
    // Helpers
    // =========================
    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String safeTrim(String s) {
        return s == null ? null : s.trim();
    }

    private AuthResponse toAuthResponse(UserEntity u) {
        Set<String> roles = (u.getRoles() == null)
                ? Set.of()
                : u.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet());

        return new AuthResponse(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                roles
        );
    }

    // =========================
    // Domain exceptions
    // =========================
    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException(String msg) { super(msg); }
    }

    public static class AccountInactiveException extends RuntimeException {
        public AccountInactiveException(String msg) { super(msg); }
    }

    public static class DuplicateEmailException extends RuntimeException {
        public DuplicateEmailException(String msg) { super(msg); }
    }

    public static class DuplicateUsernameException extends RuntimeException {
        public DuplicateUsernameException(String msg) { super(msg); }
    }

    public static class SessionNotFoundException extends RuntimeException {
        public SessionNotFoundException(String msg) { super(msg); }
    }
}
