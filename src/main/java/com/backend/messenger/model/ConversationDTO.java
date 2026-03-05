package com.backend.messenger.model;

import java.time.Instant;

/**
 * Lightweight projection returned by the conversations-list endpoint.
 */
public class ConversationDTO {
    private String partner;
    private Instant lastMessageAt;

    public ConversationDTO() {}

    public ConversationDTO(String partner, Instant lastMessageAt) {
        this.partner = partner;
        this.lastMessageAt = lastMessageAt;
    }

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }

    public Instant getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(Instant lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }
}
