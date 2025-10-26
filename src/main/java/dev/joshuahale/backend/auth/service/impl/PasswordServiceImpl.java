package dev.joshuahale.backend.auth.service.impl;

import dev.joshuahale.backend.auth.service.PasswordService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PasswordServiceImpl implements PasswordService {

    // 10–12 is fine for a personal site; raise if your server is beefy
    private static final int BCRYPT_WORK_FACTOR = 12;

    @Override
    public String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(BCRYPT_WORK_FACTOR));
    }

    @Override
    public boolean verify(String rawPassword, String passwordHash) {
        if (rawPassword == null || passwordHash == null || passwordHash.isBlank()) {
            return false;
        }
        return BCrypt.checkpw(rawPassword, passwordHash);
    }
}
