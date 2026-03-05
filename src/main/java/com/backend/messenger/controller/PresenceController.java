package com.backend.messenger.controller;

import com.backend.messenger.service.PresenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
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
    public ResponseEntity<?> isOnline(@PathVariable String username) {
        boolean online = presenceService.isOnline(username);
        return ResponseEntity.ok(Map.of("username", username, "online", online));
    }

    /**
     * Get all currently online users.
     */
    @GetMapping
    public ResponseEntity<?> onlineUsers() {
        Set<String> users = presenceService.getOnlineUsers();
        return ResponseEntity.ok(Map.of("onlineUsers", users));
    }
}
