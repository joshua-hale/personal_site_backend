package dev.joshuahale.backend.contact.entity;

import  jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table (name = "contact_messages")
public class ContactEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 200)
    private String email;

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "sent_at", nullable = false, columnDefinition = "timestamptz")
    private OffsetDateTime sentAt;

    public Long getId() {return id;}

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getEmail() {return email;}

    public void setEmail(String email) {this.email = email;}

    public String getSubject() {return subject;}

    public void setSubject(String subject) {this.subject = subject;}

    public String getMessage() {return message;}

    public void setMessage(String message) {this.message = message;}

    public OffsetDateTime getSentAt() {return sentAt;}
}
