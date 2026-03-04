package com.backend.messenger.repository;

import com.backend.messenger.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySenderAndRecipientOrRecipientAndSender(String sender1, String recipient1, String sender2, String recipient2);
}
