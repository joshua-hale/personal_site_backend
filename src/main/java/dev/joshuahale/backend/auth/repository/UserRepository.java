package dev.joshuahale.backend.auth.repository;

import dev.joshuahale.backend.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    // Login convenience: allow either email or username
    @Query("""
           select u from UserEntity u
           where lower(u.email) = lower(:login) or lower(u.username) = lower(:login)
           """)
    Optional<UserEntity> findByEmailOrUsername(@Param("login") String login);

    // Eagerly fetch roles for auth checks (avoids N+1)
    @EntityGraph(attributePaths = "roles")
    @Query("select u from UserEntity u where u.id = :id")
    Optional<UserEntity> findByIdWithRoles(@Param("id") Long id);

    @EntityGraph(attributePaths = "roles")
    @Query("""
           select u from UserEntity u
           where lower(u.email) = lower(:login) or lower(u.username) = lower(:login)
           """)
    Optional<UserEntity> findByEmailOrUsernameWithRoles(@Param("login") String login);
}

