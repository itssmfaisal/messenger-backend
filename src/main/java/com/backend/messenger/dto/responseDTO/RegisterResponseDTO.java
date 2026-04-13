package com.backend.messenger.dto.responseDTO;

public class RegisterResponseDTO {
    private String username;

    public RegisterResponseDTO() {}

    public RegisterResponseDTO(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
