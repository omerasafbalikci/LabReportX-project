package com.lab.backend.report.service.concretes;

import com.lab.backend.report.utilities.exceptions.EmailSendingFailedException;
import com.lab.backend.report.utilities.exceptions.InvalidEmailFormatException;
import com.lab.backend.report.utilities.exceptions.UnexpectedException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Service for sending emails with optional attachments.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Service
@RequiredArgsConstructor
@Log4j2
public class MailService {
    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private final JavaMailSender javaMailSender;

    /**
     * Sends an email to the specified recipient with the given subject and text.
     * <p>
     * If an attachment is provided, the email will be sent as a MIME message with the attachment included.
     * </p>
     *
     * @param to             the recipient's email address
     * @param subject        the subject of the email
     * @param text           the body of the email
     * @param attachment     optional byte array for the attachment
     * @param attachmentName the name of the attachment file
     * @throws InvalidEmailFormatException if the email configuration is invalid
     * @throws EmailSendingFailedException if sending the email fails
     */
    public void sendEmail(String to, String subject, String text, byte[] attachment, String attachmentName) {
        log.info("Preparing to send email to: {}", to);
        if (fromEmail == null || fromEmail.isEmpty() || to == null || to.isEmpty()) {
            log.error("Email configuration is missing. From: {}, To: {}", fromEmail, to);
            throw new InvalidEmailFormatException("Email configuration is missing.");
        }
        if (isEmailNotValid(fromEmail)) {
            log.error("From email address format is invalid: {}", fromEmail);
            throw new InvalidEmailFormatException("From email address format is invalid: " + fromEmail);
        }
        if (isEmailNotValid(to)) {
            log.error("Email address format is invalid: {}", to);
            throw new InvalidEmailFormatException("Email address format is invalid: " + to);
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom(this.fromEmail);
            if (attachment != null && attachment.length > 0) {
                log.info("Sending email with attachment to: {}", to);
                sendEmailWithAttachment(message, attachment, attachmentName);
            } else {
                log.info("Sending email without attachment to: {}", to);
                this.javaMailSender.send(message);
            }
        } catch (MailException e) {
            log.error("Failed to send email: {}", e.getMessage());
            throw new EmailSendingFailedException("Failed to send email: " + e.getMessage());
        }
    }

    private void sendEmailWithAttachment(SimpleMailMessage message, byte[] attachment, String attachmentName) {
        MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(mimeMessage, true);
        } catch (MessagingException e) {
            log.error("Error initializing MimeMessageHelper: {}", e.getMessage());
            throw new UnexpectedException("Error initializing MimeMessageHelper: " + e.getMessage());
        }
        try {
            helper.setTo(Objects.requireNonNull(message.getTo(), "Recipient email cannot be null"));
            helper.setSubject(Objects.requireNonNull(message.getSubject(), "Email subject cannot be null"));
            helper.setText(Objects.requireNonNull(message.getText(), "Email text cannot be null"));
            helper.setFrom(Objects.requireNonNull(message.getFrom(), "Sender email cannot be null"));
            helper.addAttachment(attachmentName, new ByteArrayDataSource(attachment, "application/pdf"));
            this.javaMailSender.send(mimeMessage);
            log.info("Email with attachment sent successfully to: {}", (Object) message.getTo());
        } catch (MessagingException e) {
            log.error("Failed to send email with attachment: {}", e.getMessage());
            throw new EmailSendingFailedException("Failed to send email with attachment: " + e.getMessage());
        }
    }

    private boolean isEmailNotValid(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        return !pattern.matcher(email).matches();
    }
}
