package com.backend.messenger.ws;

import com.backend.messenger.model.Message;
import com.backend.messenger.repository.UserRepository;
import com.backend.messenger.service.MessageService;
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

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload Message message, Principal principal) {
        // Use authenticated user as sender — ignore what client sends
        message.setSender(principal.getName());

        if (userRepository.findByUsername(message.getRecipient()).isEmpty()) {
            template.convertAndSendToUser(principal.getName(), "/queue/errors",
                    Map.of("error", "User '" + message.getRecipient() + "' does not exist"));
            return;
        }

        messageService.save(message);
        template.convertAndSendToUser(message.getRecipient(), "/queue/messages", message);
        template.convertAndSendToUser(principal.getName(), "/queue/messages", message);
    }

    @MessageMapping("/chat.join")
    public void join(Principal principal) {
        template.convertAndSend("/topic/status", principal.getName() + " joined");
    }
}
