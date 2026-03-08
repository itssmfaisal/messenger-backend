package com.backend.messenger.service;

import com.backend.messenger.model.ConversationDTO;
import com.backend.messenger.model.Message;
import com.backend.messenger.model.MessageStatus;
import com.backend.messenger.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    public Message save(Message m) {
        return messageRepository.save(m);
    }

    public Optional<Message> findById(Long id) {
        return messageRepository.findById(id);
    }

    public List<Message> findConversation(String user1, String user2) {
        return messageRepository.findBySenderAndRecipientOrRecipientAndSenderOrderBySentAtAsc(user1, user2, user1, user2);
    }

    public Page<Message> findConversation(String user1, String user2, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findConversationPaginated(user1, user2, pageable);
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

    /**
     * Mark a single message as DELIVERED.
     */
    public Message markDelivered(Long messageId) {
        Message msg = messageRepository.findById(messageId).orElseThrow();
        if (msg.getStatus() == MessageStatus.SENT) {
            msg.setStatus(MessageStatus.DELIVERED);
            msg.setDeliveredAt(Instant.now());
            messageRepository.save(msg);
        }
        return msg;
    }

    /**
     * Mark a single message as SEEN.
     */
    public Message markSeen(Long messageId) {
        Message msg = messageRepository.findById(messageId).orElseThrow();
        if (msg.getStatus() != MessageStatus.SEEN) {
            if (msg.getDeliveredAt() == null) {
                msg.setDeliveredAt(Instant.now());
            }
            msg.setStatus(MessageStatus.SEEN);
            msg.setSeenAt(Instant.now());
            messageRepository.save(msg);
        }
        return msg;
    }

    /**
     * Mark all SENT messages for a recipient as DELIVERED (called when user connects).
     */
    public List<Message> markAllDelivered(String recipient) {
        List<Message> undelivered = messageRepository.findByRecipientAndStatus(recipient, MessageStatus.SENT);
        Instant now = Instant.now();
        for (Message msg : undelivered) {
            msg.setStatus(MessageStatus.DELIVERED);
            msg.setDeliveredAt(now);
        }
        return messageRepository.saveAll(undelivered);
    }
}
