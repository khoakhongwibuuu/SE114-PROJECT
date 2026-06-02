package com.carenest.backend.features.chat.controller;

import com.carenest.backend.features.community.dto.request.CreateGroupPostRequest;
import com.carenest.backend.features.community.dto.response.GroupPostResponse;
import com.carenest.backend.features.community.service.CommunityKnowledgeService;
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
public class GroupChatStompController {

    private final SimpMessagingTemplate messagingTemplate;
    private final CommunityKnowledgeService communityKnowledgeService;

    @MessageMapping("/group/{groupId}")
    public void sendGroupMessage(@DestinationVariable("groupId") Long groupId,
                            @Payload @Valid CreateGroupPostRequest request,
                            java.security.Principal principal) {

        if (principal == null) {
            log.warn("[GroupChat] Refused message from unauthenticated user (principal is null).");
            return;
        }

        if (principal instanceof Authentication authentication) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        GroupPostResponse saved = communityKnowledgeService.createGroupPost(groupId, request);
        messagingTemplate.convertAndSend("/topic/group/" + groupId, saved);
        log.info("[GroupChat] Broadcast successful: group={}, sender={}", groupId, principal.getName());
    }
}
