package com.lab.backend.auth.service.abstracts;

public interface MailService {
    void sendEmail(String to, String subject, String text);
}
