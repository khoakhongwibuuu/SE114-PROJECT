package com.carenest.backend.features.aichat.service.impl;

import com.carenest.backend.core.api.PageResponse;
import com.carenest.backend.core.exception.ResourceNotFoundException;
import com.carenest.backend.features.ai.AiGatewayClient;
import com.carenest.backend.features.aichat.dto.request.ChatRequest;
import com.carenest.backend.features.aichat.dto.response.AiChatMessageResponse;
import com.carenest.backend.features.aichat.dto.response.ChatResponse;
import com.carenest.backend.features.aichat.entity.AiChatMessage;
import com.carenest.backend.features.aichat.entity.AiChatSession;
import com.carenest.backend.features.aichat.repository.AiChatMessageRepository;
import com.carenest.backend.features.aichat.repository.AiChatSessionRepository;
import com.carenest.backend.features.aichat.service.AiChatService;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.family.util.FamilySecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProviderAiChatServiceImpl implements AiChatService {

    private final FamilySecurityUtil familySecurityUtil;
    private final AiChatSessionRepository sessionRepository;
    private final AiChatMessageRepository messageRepository;
    private final AiGatewayClient aiGatewayClient;

    @Override
    public ChatResponse sendMessage(ChatRequest request) {
        User currentUser = familySecurityUtil.getCurrentUser();
        AiChatSession session = sessionRepository
                .findByUserIdAndStatus(currentUser.getId(), "ACTIVE")
                .orElseGet(() -> sessionRepository.save(AiChatSession.builder()
                        .user(currentUser)
                        .title(createTitle(request.getMessage()))
                        .status("ACTIVE")
                        .build()));

        messageRepository.save(AiChatMessage.builder()
                .session(session)
                .role("USER")
                .content(request.getMessage())
                .build());

        ChatResponse response = aiGatewayClient.chat(request.getMessage(), session.getId());

        messageRepository.save(AiChatMessage.builder()
                .session(session)
                .role("ASSISTANT")
                .content(response.getReply())
                .build());

        response.setConversationId(session.getId());
        return response;
    }

    @Override
    public PageResponse<AiChatMessageResponse> getSessionMessages(Long sessionId, Pageable pageable) {
        User currentUser = familySecurityUtil.getCurrentUser();
        sessionRepository.findByIdAndUserId(sessionId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("AiChatSession", sessionId));
        Page<AiChatMessageResponse> page = messageRepository
                .findBySessionIdOrderByCreatedAtDesc(sessionId, pageable)
                .map(this::toResponse);
        return PageResponse.of(page);
    }

    private String createTitle(String message) {
        if (message == null || message.isBlank()) {
            return "Cuộc trò chuyện AI";
        }
        String compact = message.trim().replaceAll("\\s+", " ");
        return compact.length() > 80 ? compact.substring(0, 80) : compact;
    }

    private AiChatMessageResponse toResponse(AiChatMessage message) {
        return AiChatMessageResponse.builder()
                .id(message.getId())
                .sessionId(message.getSession() != null ? message.getSession().getId() : null)
                .role(message.getRole())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
