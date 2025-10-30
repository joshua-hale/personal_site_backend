package dev.joshuahale.backend.contact.dto;

import java.time.OffsetDateTime;

public class ContactResponse {

    private Long id;
    private OffsetDateTime sentAt;
    private String message;

    public ContactResponse(Long id, OffsetDateTime sentAt, String message) {
        this.id = id;
        this.sentAt = sentAt;
        this.message = message;
    }

    public Long getId() {return id;}

    public OffsetDateTime getSentAt() {return sentAt;}

    public String getMessage() {return message;}
}
