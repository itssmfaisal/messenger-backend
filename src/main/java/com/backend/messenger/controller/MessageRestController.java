package com.backend.messenger.controller;

import com.backend.messenger.model.Message;
import com.backend.messenger.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageRestController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/conversation/{withUser}")
    public ResponseEntity<?> conversation(@AuthenticationPrincipal UserDetails user, @PathVariable String withUser) {
        List<Message> msgs = messageService.findConversation(user.getUsername(), withUser);
        return ResponseEntity.ok(msgs);
    }
}
