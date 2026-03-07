package com.backend.messenger.controller;

import com.backend.messenger.model.ProfileDTO;
import com.backend.messenger.model.ProfileUpdateRequest;
import com.backend.messenger.model.User;
import com.backend.messenger.service.FileStorageService;
import com.backend.messenger.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired private UserService userService;
    @Autowired private FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<?> getOwnProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok(toDTO(user));
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getProfile(@PathVariable String username) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }
        return ResponseEntity.ok(toDTO(user));
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ProfileUpdateRequest request) {
        User user = userService.updateProfile(
                userDetails.getUsername(), request.getDisplayName(), request.getBio());
        return ResponseEntity.ok(toDTO(user));
    }

    @PostMapping("/picture")
    public ResponseEntity<?> uploadProfilePicture(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        try {
            String url = fileStorageService.storeProfilePicture(userDetails.getUsername(), file);
            User user = userService.updateProfilePicture(userDetails.getUsername(), url);
            return ResponseEntity.ok(toDTO(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to upload file"));
        }
    }

    private ProfileDTO toDTO(User user) {
        return new ProfileDTO(
                user.getUsername(),
                user.getDisplayName(),
                user.getBio(),
                user.getProfilePictureUrl());
    }
}
