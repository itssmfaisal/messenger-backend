package com.backend.messenger.dto.responseDTO;

import org.springframework.data.domain.Page;

import java.util.List;

public class ConversationsResponse {
    private Page<ConversationDTO> conversations;
    private List<UserDisplayNameMapping> userNameDisplayNameMapping;

    public ConversationsResponse() {}

    public ConversationsResponse(Page<ConversationDTO> conversations, List<UserDisplayNameMapping> userNameDisplayNameMapping) {
        this.conversations = conversations;
        this.userNameDisplayNameMapping = userNameDisplayNameMapping;
    }

    public Page<ConversationDTO> getConversations() {
        return conversations;
    }

    public void setConversations(Page<ConversationDTO> conversations) {
        this.conversations = conversations;
    }

    public List<UserDisplayNameMapping> getUserNameDisplayNameMapping() {
        return userNameDisplayNameMapping;
    }

    public void setUserNameDisplayNameMapping(List<UserDisplayNameMapping> userNameDisplayNameMapping) {
        this.userNameDisplayNameMapping = userNameDisplayNameMapping;
    }
}
