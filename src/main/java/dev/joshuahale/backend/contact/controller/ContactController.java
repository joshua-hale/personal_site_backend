package dev.joshuahale.backend.contact.controller;

import dev.joshuahale.backend.contact.dto.ContactRequest;
import dev.joshuahale.backend.contact.dto.ContactResponse;
import dev.joshuahale.backend.contact.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<ContactResponse> submitContactMessage(@Valid @RequestBody ContactRequest request) {
        ContactResponse response = contactService.submitContactMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
