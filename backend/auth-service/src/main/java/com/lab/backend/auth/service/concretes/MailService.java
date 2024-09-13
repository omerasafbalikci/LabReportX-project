package com.lab.backend.auth.service.concretes;

import com.lab.backend.auth.utilities.exceptions.EmailSendingFailedException;
import com.lab.backend.auth.utilities.exceptions.InvalidEmailFormatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Log4j2
public class MailService {
    @Value("${spring.mail.username}")
    private String fromEmail;
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private final JavaMailSender javaMailSender;

    public void sendEmail(String to, String subject, String text) {
        log.trace("Entering sendEmail method with parameters - to: {}, subject: {}, text length: {}", to, subject, text.length());
        if (fromEmail == null || fromEmail.isEmpty() || to == null || to.isEmpty()) {
            log.error("Email configuration is missing. From email or recipient email is null or empty.");
            throw new InvalidEmailFormatException("Email configuration is missing.");
        }
        if (isEmailNotValid(fromEmail)) {
            log.error("From email address format is invalid: {}", fromEmail);
            throw new InvalidEmailFormatException("From email address format is invalid: " + fromEmail);
        }
        if (isEmailNotValid(to)) {
            log.warn("Email address format is invalid: {}", to);
            throw new InvalidEmailFormatException("Email address format is invalid: " + to);
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom(this.fromEmail);
            log.info("Sending email to {} with subject {}", to, subject);
            this.javaMailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (MailException e) {
            log.error("Failed to send email to {}. Exception: {}", to, e.getMessage(), e);
            throw new EmailSendingFailedException("Failed to send email: " + e.getMessage());
        }
        log.trace("Exiting sendEmail method");
    }

    private boolean isEmailNotValid(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        return !pattern.matcher(email).matches();
    }
}
