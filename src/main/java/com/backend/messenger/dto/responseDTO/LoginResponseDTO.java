package com.backend.messenger.dto.responseDTO;

public class LoginResponseDTO {
    private String token;

    public LoginResponseDTO() {}

    public LoginResponseDTO(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
