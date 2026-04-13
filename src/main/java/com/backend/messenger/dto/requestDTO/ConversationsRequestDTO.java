package com.backend.messenger.dto.requestDTO;

public class ConversationsRequestDTO {
    private int page;
    private int size;

    public ConversationsRequestDTO() {}

    public ConversationsRequestDTO(int page, int size) {
        this.page = page;
        this.size = size;
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
