package com.carenest.backend.features.chat.controller;

import com.carenest.backend.core.exception.ResourceNotFoundException;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.chat.dto.request.SendMessageRequest;
import com.carenest.backend.features.chat.dto.response.ChatMessageResponse;
import com.carenest.backend.features.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GroupChatStompController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final UserRepository userRepository;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload @Valid SendMessageRequest request,
                            java.security.Principal principal) {

        if (principal == null) {
            log.warn("[Chat] Refused message from unauthenticated user (principal is null).");
            return;
        }

        String email = principal.getName();
        log.info("[Chat] Received STOMP message from: {}", email);

        User fullSender = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        ChatMessageResponse saved = chatService.saveMessage(
                request.getFamilyId(),
                fullSender.getId(),
                request.getContent()
        );

        messagingTemplate.convertAndSend(
                "/topic/family/" + request.getFamilyId(),
                saved
        );

        log.info("[Chat] Broadcast successful: family={}, sender={}", request.getFamilyId(), fullSender.getEmail());
    }
}
