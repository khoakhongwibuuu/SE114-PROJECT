package com.carenest.backend.features.aichat.service;

import com.carenest.backend.core.api.PageResponse;
import com.carenest.backend.features.aichat.dto.request.ChatRequest;
import com.carenest.backend.features.aichat.dto.response.AiChatMessageResponse;
import com.carenest.backend.features.aichat.dto.response.ChatResponse;
import org.springframework.data.domain.Pageable;

public interface AiChatService {
    /**
     * Nhận tin nhắn từ user, tiêm bối cảnh y tế từ DB, gọi LLM và trả về phản hồi an toàn.
     */
    ChatResponse sendMessage(ChatRequest request);

    PageResponse<AiChatMessageResponse> getSessionMessages(Long sessionId, Pageable pageable);
}
