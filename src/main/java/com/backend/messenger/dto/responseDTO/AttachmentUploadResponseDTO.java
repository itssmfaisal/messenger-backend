package com.backend.messenger.dto.responseDTO;

public class AttachmentUploadResponseDTO {
    private String attachmentUrl;
    private String attachmentName;
    private String attachmentType;
    private long attachmentSize;

    public AttachmentUploadResponseDTO() {}

    public AttachmentUploadResponseDTO(String attachmentUrl, String attachmentName, String attachmentType, long attachmentSize) {
        this.attachmentUrl = attachmentUrl;
        this.attachmentName = attachmentName;
        this.attachmentType = attachmentType;
        this.attachmentSize = attachmentSize;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public long getAttachmentSize() {
        return attachmentSize;
    }

    public void setAttachmentSize(long attachmentSize) {
        this.attachmentSize = attachmentSize;
    }
}
