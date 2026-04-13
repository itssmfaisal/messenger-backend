package com.backend.messenger.controller;

import com.backend.messenger.dto.requestDTO.ForgotPasswordRequestDTO;
import com.backend.messenger.dto.requestDTO.LoginRequestDTO;
import com.backend.messenger.dto.requestDTO.RegisterRequestDTO;
import com.backend.messenger.dto.requestDTO.ResetPasswordRequestDTO;
import com.backend.messenger.dto.requestDTO.VerifyOtpRequestDTO;
import com.backend.messenger.dto.responseDTO.ErrorResponseDTO;
import com.backend.messenger.dto.responseDTO.LoginResponseDTO;
import com.backend.messenger.dto.responseDTO.MessageResponseDTO;
import com.backend.messenger.dto.responseDTO.RegisterResponseDTO;
import com.backend.messenger.model.User;
import com.backend.messenger.security.JwtUtil;
import com.backend.messenger.service.PasswordResetService;
import com.backend.messenger.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody RegisterRequestDTO request) {
        String username = request.getUsername();
        String email = request.getEmail();
        String password = request.getPassword();
        try {
            User u = userService.register(username, email, password);
            return ResponseEntity.ok(new RegisterResponseDTO(u.getUsername()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponseDTO(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequestDTO request) {
        try {
            String username = request.getUsername();
            String password = request.getPassword();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            String token = jwtUtil.generateToken(username);
            return ResponseEntity.ok(new LoginResponseDTO(token));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body(new ErrorResponseDTO("Invalid credentials"));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Object> forgotPassword(@RequestBody ForgotPasswordRequestDTO request) {
        try {
            String email = request.getEmail();
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body(new ErrorResponseDTO("Email is required"));
            }
            passwordResetService.sendOtp(email);
            return ResponseEntity.ok(new MessageResponseDTO("OTP sent to your email"));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponseDTO(ex.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Object> verifyOtp(@RequestBody VerifyOtpRequestDTO request) {
        String email = request.getEmail();
        String otp = request.getOtp();
        if (email == null || otp == null) {
            return ResponseEntity.badRequest().body(new ErrorResponseDTO("Email and OTP are required"));
        }
        boolean valid = passwordResetService.verifyOtp(email, otp);
        if (valid) {
            return ResponseEntity.ok(new MessageResponseDTO("OTP verified successfully"));
        } else {
            return ResponseEntity.badRequest().body(new ErrorResponseDTO("Invalid or expired OTP"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Object> resetPassword(@RequestBody ResetPasswordRequestDTO request) {
        try {
            String email = request.getEmail();
            String otp = request.getOtp();
            String newPassword = request.getNewPassword();
            if (email == null || otp == null || newPassword == null) {
                return ResponseEntity.badRequest().body(new ErrorResponseDTO("Email, OTP, and new password are required"));
            }
            passwordResetService.resetPassword(email, otp, newPassword);
            return ResponseEntity.ok(new MessageResponseDTO("Password reset successfully"));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponseDTO(ex.getMessage()));
        }
    }
}
