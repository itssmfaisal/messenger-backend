package com.backend.messenger.repository;

import com.backend.messenger.model.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {
    Optional<PasswordResetOtp> findTopByEmailAndUsedFalseOrderByExpiresAtDesc(String email);
    void deleteByEmail(String email);
}
