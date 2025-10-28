-- Seed roles (safety check)
INSERT INTO roles (name)
VALUES ('USER'), ('ADMIN')
    ON CONFLICT (name) DO NOTHING;

-- Seed admin user
INSERT INTO users (email, username, password_hash)
VALUES (
           'joshuahale173@gmail.com',
           'joshuahale',
           '$2a$12$T7rD5Kym9qu.8VdT6DmLQOMR9IfD1rZWuWz4qR4L2yMqB7Sik/F2G'  -- bcrypt hash for 'mySecurePassword123'
       )
    ON CONFLICT (email) DO NOTHING;

-- Link user to ADMIN role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
         JOIN roles r ON r.name = 'ADMIN'
WHERE u.username = 'joshua'
    ON CONFLICT DO NOTHING;
