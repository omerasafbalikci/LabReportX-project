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

@Service
@RequiredArgsConstructor
@Log4j2
public class MailService {
    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private final JavaMailSender javaMailSender;

    public void sendEmail(String to, String subject, String text, byte[] attachment, String attachmentName) {
        if (fromEmail == null || fromEmail.isEmpty() || to == null || to.isEmpty()) {
            throw new InvalidEmailFormatException("Email configuration is missing.");
        }
        if (isEmailNotValid(fromEmail)) {
            throw new InvalidEmailFormatException("From email address format is invalid: " + fromEmail);
        }
        if (isEmailNotValid(to)) {
            throw new InvalidEmailFormatException("Email address format is invalid: " + to);
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom(this.fromEmail);
            if (attachment != null && attachment.length > 0) {
                sendEmailWithAttachment(message, attachment, attachmentName);
            } else {
                this.javaMailSender.send(message);
            }
        } catch (MailException e) {
            throw new EmailSendingFailedException("Failed to send email: " + e.getMessage());
        }
    }

    private void sendEmailWithAttachment(SimpleMailMessage message, byte[] attachment, String attachmentName) {
        MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(mimeMessage, true);
        } catch (MessagingException e) {
            throw new UnexpectedException("Error initializing MimeMessageHelper: " + e.getMessage());
        }
        try {
            helper.setTo(Objects.requireNonNull(message.getTo(), "Recipient email cannot be null"));
            helper.setSubject(Objects.requireNonNull(message.getSubject(), "Email subject cannot be null"));
            helper.setText(Objects.requireNonNull(message.getText(), "Email text cannot be null"));
            helper.setFrom(Objects.requireNonNull(message.getFrom(), "Sender email cannot be null"));
            helper.addAttachment(attachmentName, new ByteArrayDataSource(attachment, "application/pdf"));
            this.javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new EmailSendingFailedException("Failed to send email with attachment: " + e.getMessage());
        }
    }

    private boolean isEmailNotValid(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        return !pattern.matcher(email).matches();
    }
}
