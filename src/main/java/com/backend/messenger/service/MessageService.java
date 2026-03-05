package com.backend.messenger.service;

import com.backend.messenger.model.ConversationDTO;
import com.backend.messenger.model.Message;
import com.backend.messenger.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    public Message save(Message m) {
        return messageRepository.save(m);
    }

    public List<Message> findConversation(String user1, String user2) {
        return messageRepository.findBySenderAndRecipientOrRecipientAndSender(user1, user2, user1, user2);
    }

    /**
     * Returns a paginated list of conversation partners for the given user,
     * ordered by the most recent message timestamp (newest first).
     */
    public Page<ConversationDTO> findConversations(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> raw = messageRepository.findConversationPartners(username, pageable);
        return raw.map(row -> {
            String partner = (String) row[0];
            Instant lastMessageAt;
            if (row[1] instanceof Timestamp ts) {
                lastMessageAt = ts.toInstant();
            } else {
                lastMessageAt = (Instant) row[1];
            }
            return new ConversationDTO(partner, lastMessageAt);
        });
    }
}
