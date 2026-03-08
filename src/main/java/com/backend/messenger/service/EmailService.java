package com.backend.messenger.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) {
        String subject = "Password Reset OTP";
        String heading = "Password Reset";
        String description = "We received a request to reset your password. Use the OTP code below to proceed.";
        String footer = "If you did not request a password reset, you can safely ignore this email.";
        sendHtmlOtpEmail(toEmail, subject, heading, description, otp, footer);
    }

    public void sendEmailVerificationOtp(String toEmail, String otp) {
        String subject = "Email Verification OTP";
        String heading = "Verify Your Email";
        String description = "Please use the OTP code below to verify your email address.";
        String footer = "If you did not request this verification, you can safely ignore this email.";
        sendHtmlOtpEmail(toEmail, subject, heading, description, otp, footer);
    }

    private void sendHtmlOtpEmail(String toEmail, String subject, String heading, String description, String otp, String footer) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);

            String html = buildOtpEmailHtml(heading, description, otp, footer);
            helper.setText(html, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildOtpEmailHtml(String heading, String description, String otp, String footer) {
        // Split OTP into individual digits for styled boxes
        StringBuilder otpBoxes = new StringBuilder();
        for (char digit : otp.toCharArray()) {
            otpBoxes.append(
                "<span style=\"display:inline-block;width:44px;height:52px;margin:0 4px;" +
                "background-color:#1a1a2e;color:#00BFA5;font-size:28px;font-weight:700;" +
                "line-height:52px;text-align:center;border-radius:10px;border:2px solid #00BFA5;" +
                "font-family:'Courier New',Courier,monospace;\">" + digit + "</span>"
            );
        }

        return "<!DOCTYPE html>" +
            "<html lang=\"en\">" +
            "<head><meta charset=\"UTF-8\"></head>" +
            "<body style=\"margin:0;padding:0;background-color:#0f0f1a;font-family:'Segoe UI',Roboto,Helvetica,Arial,sans-serif;\">" +
            "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color:#0f0f1a;padding:40px 0;\">" +
            "<tr><td align=\"center\">" +
            "<table role=\"presentation\" width=\"480\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color:#1a1a2e;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,191,165,0.15);\">" +

            // Header bar
            "<tr><td style=\"background:linear-gradient(135deg,#00BFA5,#00897B);padding:30px 40px;text-align:center;\">" +
            "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:0 auto;\"><tr>" +
            "<td style=\"padding-right:12px;vertical-align:middle;\">" +
            "<div style=\"width:40px;height:40px;background-color:rgba(255,255,255,0.2);border-radius:10px;text-align:center;line-height:40px;\">" +
            "<span style=\"font-size:22px;\">&#9993;</span></div></td>" +
            "<td style=\"vertical-align:middle;\">" +
            "<span style=\"color:#ffffff;font-size:24px;font-weight:700;letter-spacing:0.5px;\">Messenger</span>" +
            "</td></tr></table>" +
            "</td></tr>" +

            // Body
            "<tr><td style=\"padding:40px;\">" +

            // Heading
            "<h1 style=\"color:#ffffff;font-size:22px;font-weight:600;margin:0 0 12px 0;text-align:center;\">" + heading + "</h1>" +

            // Description
            "<p style=\"color:#8892b0;font-size:15px;line-height:1.6;margin:0 0 32px 0;text-align:center;\">" + description + "</p>" +

            // OTP code container
            "<div style=\"text-align:center;margin:0 0 32px 0;\">" +
            "<p style=\"color:#8892b0;font-size:12px;text-transform:uppercase;letter-spacing:2px;margin:0 0 16px 0;\">Your verification code</p>" +
            "<div style=\"display:inline-block;\">" + otpBoxes.toString() + "</div>" +
            "</div>" +

            // Timer notice
            "<div style=\"background-color:rgba(0,191,165,0.08);border:1px solid rgba(0,191,165,0.2);border-radius:10px;padding:14px 20px;text-align:center;margin:0 0 28px 0;\">" +
            "<span style=\"color:#00BFA5;font-size:14px;\">&#9200; This code expires in <strong>5 minutes</strong></span>" +
            "</div>" +

            // Footer text
            "<p style=\"color:#4a5568;font-size:13px;line-height:1.5;text-align:center;margin:0;\">" + footer + "</p>" +

            "</td></tr>" +

            // Bottom bar
            "<tr><td style=\"background-color:#141425;padding:20px 40px;text-align:center;border-top:1px solid rgba(255,255,255,0.05);\">" +
            "<p style=\"color:#4a5568;font-size:12px;margin:0;\">This is an automated message from <span style=\"color:#00BFA5;\">Messenger</span>. Please do not reply.</p>" +
            "</td></tr>" +

            "</table>" +
            "</td></tr></table>" +
            "</body></html>";
    }
}
