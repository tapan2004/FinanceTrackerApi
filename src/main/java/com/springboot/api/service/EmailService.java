package com.springboot.api.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${mail.from}")
    private String fromEmail;
    private final JavaMailSender mailSender;

    @Async
    public void sendEmail(String to, String subject, String textContent) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(textContent);
        message.setFrom(fromEmail);
        mailSender.send(message);
    }

    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("Email sent to {}", to);

        } catch (Exception e) {
            log.error(" Email failed: {}", e.getMessage());
            throw new RuntimeException("Email sending failed: " + e.getMessage());
        }
    }

    @Async
    public void sendEmailWithAttachment(
            String to,
            String subject,
            String text,
            ByteArrayInputStream attachment,
            String fileName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);
            helper.setFrom(fromEmail);
            helper.addAttachment(
                    fileName,
                    () -> attachment
            );
            mailSender.send(message);
            log.info("Email with attachment sent to {}", to);
        } catch (Exception e) {
            log.error("Email failed: {}", e.getMessage());
            throw new RuntimeException("Email sending failed");
        }
    }
}