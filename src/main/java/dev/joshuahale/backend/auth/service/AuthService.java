package dev.joshuahale.backend.auth.service;

import dev.joshuahale.backend.auth.dto.AuthResponse;
import dev.joshuahale.backend.auth.dto.LoginRequest;
import dev.joshuahale.backend.auth.dto.SignupRequest;

import java.util.Optional;

public interface AuthService {
    AuthResponse register(SignupRequest request);
    AuthResponse login(LoginRequest request, String userAgent, String ipAddress);
    void logout(String sessionToken);
    void logoutAll(Long userId);
    Optional<AuthResponse> currentUser(String sessionToken);
}
