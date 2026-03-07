package com.backend.messenger.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp");

    private static final Set<String> ALLOWED_ATTACHMENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain",
            "application/zip",
            "audio/mpeg", "audio/wav",
            "video/mp4", "video/webm");

    private static final long MAX_PROFILE_PICTURE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final long MAX_ATTACHMENT_SIZE = 25 * 1024 * 1024; // 25MB

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private Path profilePicturesPath;
    private Path attachmentsPath;

    @PostConstruct
    public void init() throws IOException {
        profilePicturesPath = Paths.get(uploadDir, "profile-pictures");
        attachmentsPath = Paths.get(uploadDir, "attachments");
        Files.createDirectories(profilePicturesPath);
        Files.createDirectories(attachmentsPath);
    }

    public String storeProfilePicture(String username, MultipartFile file) throws IOException {
        validateFile(file, ALLOWED_IMAGE_TYPES, MAX_PROFILE_PICTURE_SIZE);

        String extension = getExtension(file.getOriginalFilename());
        String filename = username + "_" + UUID.randomUUID() + extension;
        Path target = profilePicturesPath.resolve(filename).normalize();

        if (!target.startsWith(profilePicturesPath)) {
            throw new IOException("Invalid file path");
        }

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return "/files/profile-pictures/" + filename;
    }

    public String storeAttachment(MultipartFile file) throws IOException {
        validateFile(file, ALLOWED_ATTACHMENT_TYPES, MAX_ATTACHMENT_SIZE);

        String extension = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + extension;
        Path target = attachmentsPath.resolve(filename).normalize();

        if (!target.startsWith(attachmentsPath)) {
            throw new IOException("Invalid file path");
        }

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return "/files/attachments/" + filename;
    }

    private void validateFile(MultipartFile file, Set<String> allowedTypes, long maxSize) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size");
        }
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new IllegalArgumentException("File type not allowed: " + contentType);
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : "";
    }
}
