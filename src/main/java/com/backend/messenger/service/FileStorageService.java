package com.backend.messenger.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

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

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

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

    @Value("${file.storage.type:local}")
    private String storageType;

    @Value("${aws.region:}")
    private String awsRegion;

    @Value("${aws.s3.bucket:}")
    private String s3Bucket;

    private Path profilePicturesPath;
    private Path attachmentsPath;
    private S3Client s3Client;

    @PostConstruct
    public void init() throws IOException {
        if (isS3Storage()) {
            if (awsRegion == null || awsRegion.isBlank()) {
                throw new IllegalStateException("aws.region must be configured when file.storage.type=s3");
            }
            if (s3Bucket == null || s3Bucket.isBlank()) {
                throw new IllegalStateException("aws.s3.bucket must be configured when file.storage.type=s3");
            }

            s3Client = S3Client.builder()
                    .region(Region.of(awsRegion))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
                log.info("File storage backend: s3 (bucket={}, region={})", s3Bucket, awsRegion);
            return;
        }

        profilePicturesPath = Paths.get(uploadDir, "profile-pictures");
        attachmentsPath = Paths.get(uploadDir, "attachments");
        Files.createDirectories(profilePicturesPath);
        Files.createDirectories(attachmentsPath);
        log.info("File storage backend: local (dir={})", Paths.get(uploadDir).toAbsolutePath());
    }

    public String storeProfilePicture(String username, MultipartFile file) throws IOException {
        validateFile(file, ALLOWED_IMAGE_TYPES, MAX_PROFILE_PICTURE_SIZE);

        String extension = getExtension(file.getOriginalFilename());
        String filename = username + "_" + UUID.randomUUID() + extension;

        if (isS3Storage()) {
            String key = "profile-pictures/" + filename;
            putObjectToS3(file, key);
            return buildS3Url(key);
        }

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

        if (isS3Storage()) {
            String key = "attachments/" + filename;
            putObjectToS3(file, key);
            return buildS3Url(key);
        }

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

    private boolean isS3Storage() {
        return storageType != null && "s3".equalsIgnoreCase(storageType.trim());
    }

    private void putObjectToS3(MultipartFile file, String key) throws IOException {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(s3Bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (S3Exception e) {
            throw new IOException("Failed to upload file to S3", e);
        }
    }

    private String buildS3Url(String key) {
        return "https://" + s3Bucket + ".s3." + awsRegion + ".amazonaws.com/" + key;
    }
}
