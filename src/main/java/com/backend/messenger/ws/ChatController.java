package com.backend.messenger.ws;

import com.backend.messenger.model.Message;
import com.backend.messenger.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private MessageService messageService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload Message message) {
        messageService.save(message);
        template.convertAndSendToUser(message.getRecipient(), "/queue/messages", message);
        template.convertAndSendToUser(message.getSender(), "/queue/messages", message);
        template.convertAndSend("/topic/messages", message);
    }

    @MessageMapping("/chat.join")
    public void join(SimpMessageHeaderAccessor headerAccessor, @Payload Message message) {
        headerAccessor.getSessionAttributes().put("username", message.getSender());
        template.convertAndSend("/topic/status", message.getSender() + " joined");
    }
}
