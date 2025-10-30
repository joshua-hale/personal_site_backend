package dev.joshuahale.backend.contact.repository;

import dev.joshuahale.backend.contact.entity.ContactEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<ContactEntity, Long> {

}
