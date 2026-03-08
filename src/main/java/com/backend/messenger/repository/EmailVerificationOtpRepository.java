package com.backend.messenger.repository;

import com.backend.messenger.model.EmailVerificationOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationOtpRepository extends JpaRepository<EmailVerificationOtp, Long> {
    Optional<EmailVerificationOtp> findTopByUsernameAndUsedFalseOrderByExpiresAtDesc(String username);
    void deleteByUsername(String username);
}
