package dev.joshuahale.backend.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import dev.joshuahale.backend.auth.entity.RoleEntity;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByName(String name);

    boolean existsByName(String name);
}
