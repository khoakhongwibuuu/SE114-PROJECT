package com.carenest.backend.features.chat.controller;

import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.chat.dto.request.SendGroupMessageRequest;
import com.carenest.backend.features.chat.dto.response.ChatMessageResponse;
import com.carenest.backend.features.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GroupChatStompController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final UserRepository userRepository;

    @MessageMapping("/group/{groupId}")
    public void sendGroupMessage(@DestinationVariable("groupId") Long groupId,
                                 @Payload @Valid SendGroupMessageRequest request,
                                 Principal principal) {
        User sender = resolveUser(principal);
        if (sender == null) {
            log.warn("[GroupChat] Refused message from unauthenticated user.");
            return;
        }

        ChatMessageResponse saved = chatService.saveGroupMessage(groupId, sender.getId(), request.getContent());
        messagingTemplate.convertAndSend("/topic/group/" + groupId, saved);
        log.info("[GroupChat] Broadcast successful: group={}, sender={}", groupId, sender.getId());
    }

    private User resolveUser(Principal principal) {
        if (principal == null) {
            return null;
        }
        if (principal instanceof UsernamePasswordAuthenticationToken token) {
            Object tokenPrincipal = token.getPrincipal();
            if (tokenPrincipal instanceof User user) {
                return user;
            }
            if (tokenPrincipal instanceof UserDetails userDetails) {
                return userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            }
        }
        return userRepository.findByEmail(principal.getName()).orElse(null);
    }
}
