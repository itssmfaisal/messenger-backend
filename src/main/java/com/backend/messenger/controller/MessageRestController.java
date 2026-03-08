package com.backend.messenger.controller;

import com.backend.messenger.model.ConversationDTO;
import com.backend.messenger.model.Message;
import com.backend.messenger.service.FileStorageService;
import com.backend.messenger.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/messages")
public class MessageRestController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private FileStorageService fileStorageService;

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
    public ResponseEntity<Page<Message>> conversation(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable String withUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Message> msgs = messageService.findConversation(user.getUsername(), withUser, page, size);
        return ResponseEntity.ok(msgs);
    }

    @PostMapping("/attachment")
    public ResponseEntity<?> uploadAttachment(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam("file") MultipartFile file) {
        try {
            String url = fileStorageService.storeAttachment(file);
            return ResponseEntity.ok(Map.of(
                    "attachmentUrl", url,
                    "attachmentName", file.getOriginalFilename() != null ? file.getOriginalFilename() : "file",
                    "attachmentType", file.getContentType() != null ? file.getContentType() : "application/octet-stream",
                    "attachmentSize", file.getSize()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to upload file"));
        }
    }
}
