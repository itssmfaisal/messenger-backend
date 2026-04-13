package com.backend.messenger.dto.requestDTO;

import org.springframework.web.multipart.MultipartFile;

public class UploadAttachmentRequestDTO {
    private MultipartFile file;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}
