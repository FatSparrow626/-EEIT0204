package com.project.HR.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Sends a simple plain text email.
     *
     * @param to      The recipient's email address.
     * @param subject The subject of the email.
     * @param text    The body content of the email.
     */
    public void sendSimpleMail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            // You can set the 'from' address here if needed, otherwise it will use the one from properties.
            // message.setFrom("noreply@example.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            logger.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            logger.error("Error while sending email to {}: {}", to, e.getMessage());
            // We log the error but don't re-throw it to avoid rolling back the main transaction.
        }
    }
}
