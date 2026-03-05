package com.backend.messenger.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which users are currently connected via WebSocket.
 */
@Service
public class PresenceService {

    /** username → last-seen timestamp (present = online) */
    private final Map<String, Instant> onlineUsers = new ConcurrentHashMap<>();

    public void userConnected(String username) {
        onlineUsers.put(username, Instant.now());
    }

    public void userDisconnected(String username) {
        onlineUsers.remove(username);
    }

    public boolean isOnline(String username) {
        return onlineUsers.containsKey(username);
    }

    public Set<String> getOnlineUsers() {
        return onlineUsers.keySet();
    }

    public Instant getLastSeen(String username) {
        return onlineUsers.get(username);
    }
}
