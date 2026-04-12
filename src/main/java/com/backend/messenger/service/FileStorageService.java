package com.backend.messenger.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
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

    private static final long MAX_IMAGE_UPLOAD_SIZE = 10 * 1024 * 1024; // 10MB for upload
    private static final long MAX_COMPRESSED_IMAGE_SIZE = 1024 * 1024; // 1MB for compressed image
    private static final long MAX_PROFILE_PICTURE_SIZE = 10 * 1024 * 1024; // 10MB
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

        // Compress image to ensure it's under 1024 KB
        byte[] compressedImage = compressImage(file.getInputStream(), file.getContentType());
        
        String filename = username + "_" + UUID.randomUUID() + ".jpg";
        Path target = profilePicturesPath.resolve(filename).normalize();

        if (!target.startsWith(profilePicturesPath)) {
            throw new IOException("Invalid file path");
        }

        Files.write(target, compressedImage, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
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

        // If it's an image, compress it to ensure it's under 1024 KB
        if (isImageType(file.getContentType())) {
            byte[] compressedImage = compressImage(file.getInputStream(), file.getContentType());
            Files.write(target, compressedImage, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } else {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        }

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

    private boolean isImageType(String contentType) {
        return contentType != null && ALLOWED_IMAGE_TYPES.contains(contentType);
    }

    /**
     * Compresses an image to ensure it's under 1024 KB.
     * Uses adaptive quality adjustment to meet the size constraint.
     */
    private byte[] compressImage(java.io.InputStream inputStream, String contentType) throws IOException {
        BufferedImage originalImage = ImageIO.read(inputStream);
        if (originalImage == null) {
            throw new IOException("Could not read image");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        float quality = 0.85f; // Start with 85% quality
        byte[] compressedImage;

        // Iteratively compress with decreasing quality until under 1MB
        while (true) {
            outputStream.reset();

            Iterator<ImageWriter> jpgWriters = ImageIO.getImageWritersByFormatName("jpg");
            if (!jpgWriters.hasNext()) {
                // Fallback: use PNG if JPEG writer not available
                ImageIO.write(originalImage, "png", outputStream);
            } else {
                ImageWriter jpgWriter = jpgWriters.next();
                ImageOutputStream imageOutput = ImageIO.createImageOutputStream(outputStream);
                jpgWriter.setOutput(imageOutput);

                javax.imageio.IIOImage iioImage = new javax.imageio.IIOImage(originalImage, null, null);
                javax.imageio.ImageWriteParam writeParam = jpgWriter.getDefaultWriteParam();
                writeParam.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
                writeParam.setCompressionQuality(quality);

                jpgWriter.write(null, iioImage, writeParam);
                jpgWriter.dispose();
                imageOutput.close();
            }

            compressedImage = outputStream.toByteArray();

            // Check if compressed image is under 1MB, or if quality is too low to compress further
            if (compressedImage.length <= MAX_COMPRESSED_IMAGE_SIZE || quality <= 0.1f) {
                break;
            }

            quality -= 0.05f; // Reduce quality by 5%
        }

        if (compressedImage.length > MAX_COMPRESSED_IMAGE_SIZE) {
            throw new IOException("Unable to compress image to under 1MB");
        }

        return compressedImage;
    }
}
