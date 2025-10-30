package dev.joshuahale.backend.contact.service;

import dev.joshuahale.backend.contact.dto.ContactRequest;
import dev.joshuahale.backend.contact.dto.ContactResponse;
import dev.joshuahale.backend.contact.repository.ContactRepository;
import dev.joshuahale.backend.contact.entity.ContactEntity;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class ContactServiceImpl implements ContactService {

    private static final Logger log = LoggerFactory.getLogger(ContactServiceImpl.class);
    private final ContactRepository contactRepository;
    private final JavaMailSender mailSender;
    private final String recipientEmail;

    public ContactServiceImpl(ContactRepository contactRepository,
                              JavaMailSender mailSender,
                              @Value("${contact.recipient.email}") String recipientEmail,
                              @Value("${contact.from.email}") String fromEmail,
                              @Value("${contact.from.name:Portfolio Contact Form}") String fromName) {
        this.contactRepository = contactRepository;
        this.mailSender = mailSender;
        this.recipientEmail = recipientEmail;
        this.fromEmail = fromEmail;
    }

    @Override
    @Transactional
    public ContactResponse submitContactMessage(ContactRequest request) {
        ContactEntity entity = mapToEntity(request);
        ContactEntity saved = contactRepository.save(entity);
        log.info("Contact message saved with ID: {}", saved.getId());

        try {
            sendEmail(request, saved.getSentAt());
            log.info("Contact message sent successfully");
        } catch (MessagingException e) {
            log.error("Failed to send contact email", e);
        }

        return new ContactResponse(
                saved.getId(),
                saved.getSentAt(),
                "Thank you for your message!"
        );


    }

    private ContactEntity mapToEntity(ContactRequest request) {
        ContactEntity entity = new ContactEntity();
        entity.setName(request.getName());
        entity.setEmail(request.getEmail());
        entity.setSubject(request.getSubject());
        entity.setMessage(request.getMessage());
        return entity;
    }

    private void sendEmail(ContactRequest request, OffsetDateTime sentAt) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(recipientEmail);
        helper.setReplyTo(sanitizeHeaderValue(request.getEmail()));
        helper.setSubject("Contact Form: " + sanitizeHeaderValue(request.getSubject()));


        String emailBody = buildEmailBody(request, sentAt);
        helper.setText(emailBody, true);

        mailSender.send(message);
    }



    private String buildEmailBody(ContactRequest request, OffsetDateTime sentAt) {
        String formattedDate = sentAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2>New Contact Form Submission</h2>
                <p><strong>Name:</strong> %s</p>
                <p><strong>Email:</strong> %s</p>
                <p><strong>Subject:</strong> %s</p>
                <p><strong>Sent At:</strong> %s</p>
                <hr>
                <h3>Message:</h3>
                <p>%s</p>
            </body>
            </html>
            """,
                escapeHtml(request.getName()),
                escapeHtml(request.getEmail()),
                escapeHtml(request.getSubject()),
                formattedDate,
                escapeHtml(request.getMessage()).replace("\n", "<br>")
        );
    }

    private String sanitizeHeaderValue(String value) {
        if (value == null) {
            return "";
        }
        // Remove carriage return and line feed characters
        return value.replace("\r", " ")
                .replace("\n", " ")
                .trim();
    }


    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
