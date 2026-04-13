package com.backend.messenger.controller;

import com.backend.messenger.dto.requestDTO.ConversationMessagesRequestDTO;
import com.backend.messenger.dto.requestDTO.ConversationsRequestDTO;
import com.backend.messenger.dto.requestDTO.UploadAttachmentRequestDTO;
import com.backend.messenger.dto.responseDTO.AttachmentUploadResponseDTO;
import com.backend.messenger.dto.responseDTO.ConversationDTO;
import com.backend.messenger.dto.responseDTO.ConversationMessagesResponseDTO;
import com.backend.messenger.dto.responseDTO.ConversationsResponse;
import com.backend.messenger.dto.responseDTO.ErrorResponseDTO;
import com.backend.messenger.dto.responseDTO.MessageDTO;
import com.backend.messenger.dto.responseDTO.UserDisplayNameMapping;
import com.backend.messenger.model.Message;
import com.backend.messenger.model.User;
import com.backend.messenger.repository.UserRepository;
import com.backend.messenger.service.FileStorageService;
import com.backend.messenger.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/messages")
public class MessageRestController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserRepository userRepository;

    /**
     * List all conversations the authenticated user has, paginated.
     * Each item contains the partner username and the timestamp of the last message.
     */
    @GetMapping("/conversations")
    public ResponseEntity<ConversationsResponse> conversations(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ConversationsRequestDTO request = new ConversationsRequestDTO(page, size);
        Page<ConversationDTO> result = messageService.findConversations(user.getUsername(), request.getPage(), request.getSize());

        List<UserDisplayNameMapping> mapping = result.getContent().stream()
                .map(ConversationDTO::getPartner)
                .distinct()
                .map(uname -> new UserDisplayNameMapping(
                        uname,
                        userRepository.findByUsername(uname).map(User::getDisplayName).orElse(null)
                ))
                .collect(Collectors.toList());

        ConversationsResponse resp = new ConversationsResponse(result, mapping);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/conversation/{withUser}")
    public ResponseEntity<ConversationMessagesResponseDTO> conversation(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable String withUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ConversationMessagesRequestDTO request = new ConversationMessagesRequestDTO(withUser, page, size);
        Page<MessageDTO> msgs = messageService
                .findConversation(user.getUsername(), request.getWithUser(), request.getPage(), request.getSize())
                .map(this::toMessageDTO);

        List<UserDisplayNameMapping> mapping = msgs.getContent().stream()
            .flatMap(m -> java.util.stream.Stream.of(m.getSender(), m.getRecipient()))
            .distinct()
            .map(uname -> new UserDisplayNameMapping(
                uname,
                userRepository.findByUsername(uname).map(User::getDisplayName).orElse(null)
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(new ConversationMessagesResponseDTO(msgs, mapping));
    }

    @PostMapping("/attachment")
    public ResponseEntity<Object> uploadAttachment(
            @AuthenticationPrincipal UserDetails user,
            @ModelAttribute UploadAttachmentRequestDTO request) {
        try {
            if (request.getFile() == null || request.getFile().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponseDTO("File is required"));
            }
            String url = fileStorageService.storeAttachment(request.getFile());
            AttachmentUploadResponseDTO response = new AttachmentUploadResponseDTO(
                    url,
                    request.getFile().getOriginalFilename() != null ? request.getFile().getOriginalFilename() : "file",
                    request.getFile().getContentType() != null ? request.getFile().getContentType() : "application/octet-stream",
                    request.getFile().getSize());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponseDTO(e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(new ErrorResponseDTO("Failed to upload file"));
        }
    }

    private MessageDTO toMessageDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setSender(message.getSender());
        dto.setRecipient(message.getRecipient());
        dto.setContent(message.getContent());
        dto.setStatus(message.getStatus());
        dto.setSentAt(message.getSentAt());
        dto.setDeliveredAt(message.getDeliveredAt());
        dto.setSeenAt(message.getSeenAt());
        dto.setAttachmentUrl(message.getAttachmentUrl());
        dto.setAttachmentName(message.getAttachmentName());
        dto.setAttachmentType(message.getAttachmentType());
        dto.setAttachmentSize(message.getAttachmentSize());
        return dto;
    }
}
