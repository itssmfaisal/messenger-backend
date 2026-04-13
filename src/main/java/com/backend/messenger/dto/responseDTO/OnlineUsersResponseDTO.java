package com.backend.messenger.dto.responseDTO;

import java.util.Set;

public class OnlineUsersResponseDTO {
    private Set<String> onlineUsers;

    public OnlineUsersResponseDTO() {}

    public OnlineUsersResponseDTO(Set<String> onlineUsers) {
        this.onlineUsers = onlineUsers;
    }

    public Set<String> getOnlineUsers() {
        return onlineUsers;
    }

    public void setOnlineUsers(Set<String> onlineUsers) {
        this.onlineUsers = onlineUsers;
    }
}
