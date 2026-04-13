package com.backend.messenger.dto.responseDTO;

import org.springframework.data.domain.Page;

import java.util.List;

public class ConversationMessagesResponseDTO {
    private Page<MessageDTO> messages;
    private List<UserDisplayNameMapping> userNameDisplayNameMapping;

    public ConversationMessagesResponseDTO() {}

    public ConversationMessagesResponseDTO(Page<MessageDTO> messages, List<UserDisplayNameMapping> userNameDisplayNameMapping) {
        this.messages = messages;
        this.userNameDisplayNameMapping = userNameDisplayNameMapping;
    }

    public Page<MessageDTO> getMessages() {
        return messages;
    }

    public void setMessages(Page<MessageDTO> messages) {
        this.messages = messages;
    }

    public List<UserDisplayNameMapping> getUserNameDisplayNameMapping() {
        return userNameDisplayNameMapping;
    }

    public void setUserNameDisplayNameMapping(List<UserDisplayNameMapping> userNameDisplayNameMapping) {
        this.userNameDisplayNameMapping = userNameDisplayNameMapping;
    }
}
