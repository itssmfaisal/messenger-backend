package com.backend.messenger.controller;

import com.backend.messenger.model.ConversationDTO;
import com.backend.messenger.model.Message;
import com.backend.messenger.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageRestController {

    @Autowired
    private MessageService messageService;

    /**
     * List all conversations the authenticated user has, paginated.
     * Each item contains the partner username and the timestamp of the last message.
     */
    @GetMapping("/conversations")
    public ResponseEntity<Page<ConversationDTO>> conversations(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ConversationDTO> result = messageService.findConversations(user.getUsername(), page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/conversation/{withUser}")
    public ResponseEntity<?> conversation(@AuthenticationPrincipal UserDetails user, @PathVariable String withUser) {
        List<Message> msgs = messageService.findConversation(user.getUsername(), withUser);
        return ResponseEntity.ok(msgs);
    }
}
