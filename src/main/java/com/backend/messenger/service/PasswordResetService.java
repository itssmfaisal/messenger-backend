package com.backend.messenger.service;

import com.backend.messenger.model.PasswordResetOtp;
import com.backend.messenger.model.User;
import com.backend.messenger.repository.PasswordResetOtpRepository;
import com.backend.messenger.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class PasswordResetService {

    @Autowired
    private PasswordResetOtpRepository otpRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a 6-digit OTP, store it, and send it to the user's email.
     */
    @Transactional
    public void sendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email"));

        String otp = generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpirationMinutes);

        PasswordResetOtp resetOtp = new PasswordResetOtp(email, otp, expiresAt);
        otpRepository.save(resetOtp);

        emailService.sendOtpEmail(email, otp);
    }

    /**
     * Verify the OTP code for the given email.
     * Returns true if valid and not expired.
     */
    public boolean verifyOtp(String email, String otp) {
        PasswordResetOtp resetOtp = otpRepository
                .findTopByEmailAndUsedFalseOrderByExpiresAtDesc(email)
                .orElse(null);

        if (resetOtp == null) {
            return false;
        }

        if (resetOtp.isExpired()) {
            return false;
        }

        return resetOtp.getOtp().equals(otp);
    }

    /**
     * Reset the user's password after OTP verification.
     */
    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        PasswordResetOtp resetOtp = otpRepository
                .findTopByEmailAndUsedFalseOrderByExpiresAtDesc(email)
                .orElseThrow(() -> new RuntimeException("No valid OTP found"));

        if (resetOtp.isExpired()) {
            throw new RuntimeException("OTP has expired");
        }

        if (!resetOtp.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark OTP as used
        resetOtp.setUsed(true);
        otpRepository.save(resetOtp);
    }

    private String generateOtp() {
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }
}
