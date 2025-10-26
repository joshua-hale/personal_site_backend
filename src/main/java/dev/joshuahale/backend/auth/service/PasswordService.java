package dev.joshuahale.backend.auth.service;

public interface PasswordService {
    String hash(String rawPassword);
    boolean verify(String rawPassword, String passwordHash);
}
