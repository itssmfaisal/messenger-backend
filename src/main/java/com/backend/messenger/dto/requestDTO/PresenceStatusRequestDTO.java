package com.backend.messenger.dto.requestDTO;

public class PresenceStatusRequestDTO {
    private String username;

    public PresenceStatusRequestDTO() {}

    public PresenceStatusRequestDTO(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
