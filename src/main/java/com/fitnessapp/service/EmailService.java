package com.fitnessapp.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        // Можете да добавите message.setFrom("your_email@example.com"); ако искате да е различно от username в properties

        try {
            mailSender.send(message);
            log.info("Имейл изпратен до: {} със subject: {}", to, subject);
        } catch (MailException e) {
            log.error("Грешка при изпращане на имейл до {}: {}", to, e.getMessage(), e);
            // Можете да хвърлите custom exception тук или да логнете
        }
    }
}