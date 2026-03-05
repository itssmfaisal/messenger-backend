package com.backend.messenger.ws;

import com.backend.messenger.model.Message;
import com.backend.messenger.service.MessageService;
import com.backend.messenger.service.PresenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Listens for WebSocket connect/disconnect events to track user presence
 * and auto-deliver pending messages.
 */
@Component
public class WebSocketEventListener {

    @Autowired
    private PresenceService presenceService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private SimpMessagingTemplate template;

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        Principal principal = event.getUser();
        if (principal != null) {
            String username = principal.getName();
            presenceService.userConnected(username);

            // Broadcast online status
            template.convertAndSend("/topic/presence",
                    (Object) Map.of("username", username, "online", true));

            // Auto-mark all pending SENT messages as DELIVERED
            List<Message> delivered = messageService.markAllDelivered(username);
            // Notify each sender that their messages were delivered
            for (Message msg : delivered) {
                template.convertAndSendToUser(msg.getSender(), "/queue/status-updates",
                        Map.of("messageId", msg.getId(),
                                "status", msg.getStatus().name(),
                                "deliveredAt", msg.getDeliveredAt().toString()));
            }
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        Principal principal = event.getUser();
        if (principal != null) {
            String username = principal.getName();
            presenceService.userDisconnected(username);

            // Broadcast offline status
            template.convertAndSend("/topic/presence",
                    (Object) Map.of("username", username, "online", false));
        }
    }
}
