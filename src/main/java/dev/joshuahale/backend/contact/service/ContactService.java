package dev.joshuahale.backend.contact.service;

import dev.joshuahale.backend.contact.dto.ContactRequest;
import dev.joshuahale.backend.contact.dto.ContactResponse;

public interface ContactService {
    ContactResponse submitContactMessage(ContactRequest request);
}
