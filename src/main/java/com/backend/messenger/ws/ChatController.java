package com.backend.messenger.ws;

import com.backend.messenger.model.Message;
import com.backend.messenger.model.MessageStatus;
import com.backend.messenger.repository.UserRepository;
import com.backend.messenger.service.MessageService;
import com.backend.messenger.service.PresenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PresenceService presenceService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload Message message, Principal principal) {
        // Use authenticated user as sender — ignore what client sends
        message.setSender(principal.getName());

        if (message.getContent() != null && message.getContent().length() > 2000) {
            template.convertAndSendToUser(principal.getName(), "/queue/errors",
                    Map.of("error", "Message content exceeds the maximum allowed length of 2000 characters"));
            return;
        }

        if (userRepository.findByUsername(message.getRecipient()).isEmpty()) {
            template.convertAndSendToUser(principal.getName(), "/queue/errors",
                    Map.of("error", "User '" + message.getRecipient() + "' does not exist"));
            return;
        }

        message.setStatus(MessageStatus.SENT);
        messageService.save(message);

        // If recipient is online, auto-mark as DELIVERED
        if (presenceService.isOnline(message.getRecipient())) {
            message = messageService.markDelivered(message.getId());
        }

        template.convertAndSendToUser(message.getRecipient(), "/queue/messages", message);
        template.convertAndSendToUser(principal.getName(), "/queue/messages", message);
    }

    /**
     * Client sends message IDs here to mark them as DELIVERED.
     * Payload: { "messageId": 123 }
     */
    @MessageMapping("/chat.delivered")
    public void markDelivered(@Payload Map<String, Long> payload, Principal principal) {
        Long messageId = payload.get("messageId");
        if (messageId == null) return;

        Message msg = messageService.findById(messageId).orElse(null);
        if (msg == null || !msg.getRecipient().equals(principal.getName())) return;

        msg = messageService.markDelivered(messageId);
        // Notify the sender
        template.convertAndSendToUser(msg.getSender(), "/queue/status-updates",
                Map.of("messageId", msg.getId(),
                        "status", msg.getStatus().name(),
                        "deliveredAt", msg.getDeliveredAt().toString()));
    }

    /**
     * Client sends message IDs here to mark them as SEEN.
     * Payload: { "messageId": 123 }
     */
    @MessageMapping("/chat.seen")
    public void markSeen(@Payload Map<String, Long> payload, Principal principal) {
        Long messageId = payload.get("messageId");
        if (messageId == null) return;

        Message msg = messageService.findById(messageId).orElse(null);
        if (msg == null || !msg.getRecipient().equals(principal.getName())) return;

        msg = messageService.markSeen(messageId);
        // Notify the sender
        template.convertAndSendToUser(msg.getSender(), "/queue/status-updates",
                Map.of("messageId", msg.getId(),
                        "status", msg.getStatus().name(),
                        "seenAt", msg.getSeenAt().toString()));
    }

    @MessageMapping("/chat.join")
    public void join(Principal principal) {
        template.convertAndSend("/topic/status", principal.getName() + " joined");
    }
}
