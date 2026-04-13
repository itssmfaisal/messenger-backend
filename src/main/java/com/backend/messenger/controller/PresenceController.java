package com.backend.messenger.controller;

import com.backend.messenger.dto.requestDTO.PresenceStatusRequestDTO;
import com.backend.messenger.dto.responseDTO.OnlineUsersResponseDTO;
import com.backend.messenger.dto.responseDTO.PresenceStatusResponseDTO;
import com.backend.messenger.service.PresenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/presence")
public class PresenceController {

    @Autowired
    private PresenceService presenceService;

    /**
     * Check if a specific user is online.
     */
    @GetMapping("/{username}")
    public ResponseEntity<PresenceStatusResponseDTO> isOnline(@PathVariable String username) {
        PresenceStatusRequestDTO request = new PresenceStatusRequestDTO(username);
        boolean online = presenceService.isOnline(request.getUsername());
        return ResponseEntity.ok(new PresenceStatusResponseDTO(request.getUsername(), online));
    }

    /**
     * Get all currently online users.
     */
    @GetMapping
    public ResponseEntity<OnlineUsersResponseDTO> onlineUsers() {
        Set<String> users = presenceService.getOnlineUsers();
        return ResponseEntity.ok(new OnlineUsersResponseDTO(users));
    }
}
