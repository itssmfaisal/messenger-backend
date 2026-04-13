package com.backend.messenger.controller;

import com.backend.messenger.dto.requestDTO.ProfileUpdateRequest;
import com.backend.messenger.dto.requestDTO.SendEmailVerificationOtpRequestDTO;
import com.backend.messenger.dto.requestDTO.UploadProfilePictureRequestDTO;
import com.backend.messenger.dto.requestDTO.VerifyEmailOtpRequestDTO;
import com.backend.messenger.dto.responseDTO.ErrorResponseDTO;
import com.backend.messenger.dto.responseDTO.MessageResponseDTO;
import com.backend.messenger.dto.responseDTO.ProfileDTO;
import com.backend.messenger.model.User;
import com.backend.messenger.service.EmailVerificationService;
import com.backend.messenger.service.FileStorageService;
import com.backend.messenger.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired private UserService userService;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private EmailVerificationService emailVerificationService;

    @GetMapping
    public ResponseEntity<ProfileDTO> getOwnProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok(toDTO(user));
    }

    @GetMapping("/{username}")
    public ResponseEntity<Object> getProfile(@PathVariable String username) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(404).body(new ErrorResponseDTO("User not found"));
        }
        return ResponseEntity.ok(toDTO(user));
    }

    @PutMapping
    public ResponseEntity<ProfileDTO> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ProfileUpdateRequest request) {
        User user = userService.updateProfile(
                userDetails.getUsername(), request.getDisplayName(), request.getBio());
        return ResponseEntity.ok(toDTO(user));
    }

    @PostMapping("/picture")
    public ResponseEntity<Object> uploadProfilePicture(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute UploadProfilePictureRequestDTO request) {
        try {
            if (request.getFile() == null || request.getFile().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponseDTO("File is required"));
            }
            String url = fileStorageService.storeProfilePicture(userDetails.getUsername(), request.getFile());
            User user = userService.updateProfilePicture(userDetails.getUsername(), url);
            return ResponseEntity.ok(toDTO(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponseDTO(e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(new ErrorResponseDTO("Failed to upload file"));
        }
    }

    private ProfileDTO toDTO(User user) {
        return new ProfileDTO(
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getBio(),
                user.getProfilePictureUrl());
    }

    @PostMapping("/email/send-otp")
    public ResponseEntity<Object> sendEmailVerificationOtp(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody SendEmailVerificationOtpRequestDTO request) {
        try {
            String newEmail = request.getEmail();
            if (newEmail == null || newEmail.isBlank()) {
                return ResponseEntity.badRequest().body(new ErrorResponseDTO("Email is required"));
            }
            emailVerificationService.sendVerificationOtp(userDetails.getUsername(), newEmail);
            return ResponseEntity.ok(new MessageResponseDTO("Verification OTP sent to " + newEmail));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponseDTO(ex.getMessage()));
        }
    }

    @PostMapping("/email/verify")
    public ResponseEntity<Object> verifyAndUpdateEmail(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody VerifyEmailOtpRequestDTO request) {
        try {
            String otp = request.getOtp();
            if (otp == null || otp.isBlank()) {
                return ResponseEntity.badRequest().body(new ErrorResponseDTO("OTP is required"));
            }
            User user = emailVerificationService.verifyAndUpdateEmail(userDetails.getUsername(), otp);
            return ResponseEntity.ok(toDTO(user));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponseDTO(ex.getMessage()));
        }
    }
}
