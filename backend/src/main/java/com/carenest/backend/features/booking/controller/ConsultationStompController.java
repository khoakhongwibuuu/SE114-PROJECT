package com.carenest.backend.features.booking.controller;

import com.carenest.backend.features.booking.dto.request.SendConsultationMessageRequest;
import com.carenest.backend.features.booking.dto.response.ConsultationMessageResponse;
import com.carenest.backend.features.booking.service.ConsultationMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ConsultationStompController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ConsultationMessageService consultationMessageService;

    @MessageMapping("/consultation/thread/{threadId}")
    public void sendConsultationMessage(@DestinationVariable("threadId") Long threadId,
                                        @Payload @Valid SendConsultationMessageRequest request,
                                        java.security.Principal principal) {
        
        if (principal == null) {
            log.warn("[ConsultationChat] Refused message from unauthenticated user.");
            return;
        }

        if (principal instanceof Authentication authentication) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        try {
            ConsultationMessageResponse saved = consultationMessageService.sendMessage(threadId, request);
            messagingTemplate.convertAndSend("/topic/consultation/thread/" + threadId, saved);
            log.info("[ConsultationChat] Broadcast successful: thread={}, sender={}", threadId, principal.getName());
        } catch (Exception e) {
            log.error("[ConsultationChat] Failed to send message for thread={}, sender={}. Error: {}", threadId, principal.getName(), e.getMessage());
            ConsultationMessageResponse errorResponse = ConsultationMessageResponse.builder()
                .id(-1L)
                .threadId(threadId)
                .senderId(-1L)
                .senderName("System")
                .content("ERROR: " + e.toString())
                .createdAt(java.time.Instant.now())
                .build();
            messagingTemplate.convertAndSend("/topic/consultation/thread/" + threadId, errorResponse);
        }
    }
}
