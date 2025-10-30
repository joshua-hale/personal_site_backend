package dev.joshuahale.backend.contact.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ContactRequest {

    @NotBlank
    @Size(max = 200)
    private String name;

    @Email
    @NotBlank
    @Size(max = 200)
    private String email;

    @NotBlank
    @Size(max = 200)
    private String subject;

    @NotBlank
    @Size(max = 500)
    private String message;

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}

    public String getSubject() {return subject;}
    public void setSubject(String subject) {this.subject = subject;}

    public String getMessage() {return message;}
    public void setMessage(String message) {this.message = message;}
}
