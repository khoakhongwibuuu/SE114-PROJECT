package com.carenest.backend.features.aichat.service.impl;

import com.carenest.backend.core.api.PageResponse;
import com.carenest.backend.core.exception.BadRequestException;
import com.carenest.backend.core.exception.ResourceNotFoundException;
import com.carenest.backend.features.aichat.dto.request.ChatRequest;
import com.carenest.backend.features.aichat.dto.response.AiChatMessageResponse;
import com.carenest.backend.features.aichat.dto.response.ChatResponse;
import com.carenest.backend.features.aichat.entity.AiChatMessage;
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
public class MockAiChatServiceImpl implements AiChatService {

    private final FamilySecurityUtil familySecurityUtil;
    private final AiChatSessionRepository sessionRepository;
    private final AiChatMessageRepository messageRepository;

    @Override
    public ChatResponse sendMessage(ChatRequest request) {
        throw new BadRequestException("AI chat chua co provider that trong MVP va se duoc bat lai o phase cuoi.");
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
