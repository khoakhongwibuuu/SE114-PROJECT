package com.carenest.backend.module.aichat.service.impl;

import com.carenest.backend.common.dto.PageResponse;
import com.carenest.backend.common.exception.ResourceNotFoundException;
import com.carenest.backend.module.aichat.dto.request.ChatRequest;
import com.carenest.backend.module.aichat.dto.response.AiChatMessageResponse;
import com.carenest.backend.module.aichat.dto.response.ChatResponse;
import com.carenest.backend.module.aichat.entity.AiChatMessage;
import com.carenest.backend.module.aichat.repository.AiChatMessageRepository;
import com.carenest.backend.module.aichat.repository.AiChatSessionRepository;
import com.carenest.backend.module.aichat.service.AiChatService;
import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.family.util.FamilySecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
@RequiredArgsConstructor
public class MockAiChatServiceImpl implements AiChatService {

    private final FamilySecurityUtil familySecurityUtil;
    private final AiChatSessionRepository sessionRepository;
    private final AiChatMessageRepository messageRepository;

    @Override
    public ChatResponse sendMessage(ChatRequest request) {
        return ChatResponse.builder()
                .reply("Xin chào! Tôi là trợ lý ảo CareNest (Mock). Câu hỏi của bạn là: '"
                        + request.getMessage()
                        + "'. Vui lòng đi khám bác sĩ nếu có triệu chứng nặng.")
                .build();
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
