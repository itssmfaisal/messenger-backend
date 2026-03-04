package com.backend.messenger.service;

import com.backend.messenger.model.Message;
import com.backend.messenger.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
