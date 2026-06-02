package com.carenest.backend.features.chat.controller;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class FamilyChatStompController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final UserRepository userRepository;

    @MessageMapping("/chat.sendMessage")
    public void sendFamilyMessage(@Payload @Valid SendMessageRequest request,
                                  Principal principal) {

        log.info("[FamilyChat] STOMP call to /app/chat.sendMessage initiated.");

        if (principal == null) {
            log.warn("[FamilyChat] Refused message from unauthenticated connection (principal is null).");
            return;
        }

        log.info("[FamilyChat] STOMP Principal resolved type: {}, name: {}", principal.getClass().getName(), principal.getName());

        User user = null;
        if (principal instanceof UsernamePasswordAuthenticationToken) {
            Object principalObj = ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
            if (principalObj instanceof User) {
                user = (User) principalObj;
                log.info("[FamilyChat] UsernamePasswordAuthenticationToken principal is direct User object: id={}", user.getId());
            } else if (principalObj instanceof org.springframework.security.core.userdetails.UserDetails) {
                String email = ((org.springframework.security.core.userdetails.UserDetails) principalObj).getUsername();
                log.info("[FamilyChat] Principal is UserDetails, looking up User by email: {}", email);
                user = userRepository.findByEmail(email).orElse(null);
            }
        }

        if (user == null) {
            String email = principal.getName();
            log.info("[FamilyChat] Fallback: looking up User by principal name (email): {}", email);
            user = userRepository.findByEmail(email).orElse(null);
        }

        if (user == null) {
            log.warn("[FamilyChat] Refused message: unable to resolve User entity for username='{}'", principal.getName());
            return;
        }

        Long familyId = request.getFamilyId();
        String content = request.getContent();
        log.info("[FamilyChat] Authenticated sender: id={}, email='{}' | Target familyId: {}", user.getId(), user.getEmail(), familyId);

        try {
            // chatService.saveMessage inherently performs family membership validation
            ChatMessageResponse saved = chatService.saveMessage(familyId, user.getId(), content);

            // Broadcast message to the family topic
            String destination = "/topic/family/" + familyId;
            messagingTemplate.convertAndSend(destination, saved);
            log.info("[FamilyChat] Broadcasted successfully to destination '{}' for messageId={}", destination, saved.getId());
        } catch (Exception e) {
            log.error("[FamilyChat] Error processing/broadcasting family message: {}", e.getMessage(), e);
        }
    }
}
