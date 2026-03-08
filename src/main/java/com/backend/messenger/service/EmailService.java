package com.backend.messenger.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Password Reset OTP");
        message.setText(
            "Hello,\n\n" +
            "Your OTP code for password reset is: " + otp + "\n\n" +
            "This code will expire in 5 minutes.\n\n" +
            "If you did not request a password reset, please ignore this email.\n\n" +
            "Regards,\nMessenger App"
        );
        mailSender.send(message);
    }
}
