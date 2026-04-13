package com.backend.messenger.dto.requestDTO;

public class ConversationMessagesRequestDTO {
    private String withUser;
    private int page;
    private int size;

    public ConversationMessagesRequestDTO() {}

    public ConversationMessagesRequestDTO(String withUser, int page, int size) {
        this.withUser = withUser;
        this.page = page;
        this.size = size;
    }

    public String getWithUser() {
        return withUser;
    }

    public void setWithUser(String withUser) {
        this.withUser = withUser;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
