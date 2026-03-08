package com.backend.messenger.service;

import com.backend.messenger.model.EmailVerificationOtp;
import com.backend.messenger.model.User;
import com.backend.messenger.repository.EmailVerificationOtpRepository;
import com.backend.messenger.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class EmailVerificationService {

    @Autowired
    private EmailVerificationOtpRepository otpRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Value("${otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Send a verification OTP to the new email address.
     * The user must be authenticated (identified by username).
     */
    @Transactional
    public void sendVerificationOtp(String username, String newEmail) {
        // Check if another user already has this email
        userRepository.findByEmail(newEmail).ifPresent(existingUser -> {
            if (!existingUser.getUsername().equals(username)) {
                throw new RuntimeException("This email is already in use by another account");
            }
        });

        String otp = generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpirationMinutes);

        EmailVerificationOtp verificationOtp = new EmailVerificationOtp(username, newEmail, otp, expiresAt);
        otpRepository.save(verificationOtp);

        emailService.sendEmailVerificationOtp(newEmail, otp);
    }

    /**
     * Verify the OTP and update the user's email if valid.
     */
    @Transactional
    public User verifyAndUpdateEmail(String username, String otp) {
        EmailVerificationOtp verificationOtp = otpRepository
                .findTopByUsernameAndUsedFalseOrderByExpiresAtDesc(username)
                .orElseThrow(() -> new RuntimeException("No pending email verification found"));

        if (verificationOtp.isExpired()) {
            throw new RuntimeException("OTP has expired");
        }

        if (!verificationOtp.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        // Check again that the email is not taken
        String newEmail = verificationOtp.getNewEmail();
        userRepository.findByEmail(newEmail).ifPresent(existingUser -> {
            if (!existingUser.getUsername().equals(username)) {
                throw new RuntimeException("This email is already in use by another account");
            }
        });

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmail(newEmail);
        userRepository.save(user);

        // Mark OTP as used
        verificationOtp.setUsed(true);
        otpRepository.save(verificationOtp);

        return user;
    }

    private String generateOtp() {
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }
}
