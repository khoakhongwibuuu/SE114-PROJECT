package com.carenest.backend.features.aichat.service;

import com.carenest.backend.core.api.PageResponse;
import com.carenest.backend.features.aichat.dto.request.ChatRequest;
import com.carenest.backend.features.aichat.dto.response.AiChatMessageResponse;
import com.carenest.backend.features.aichat.dto.response.ChatResponse;
import org.springframework.data.domain.Pageable;

public interface AiChatService {
    /**
     * Nháº­n tin nháº¯n tá»« user, tiÃªm bá»‘i cáº£nh y táº¿ tá»« DB, gá»i LLM vÃ  tráº£ vá» pháº£n há»“i an toÃ n.
     */
    ChatResponse sendMessage(ChatRequest request);

    PageResponse<AiChatMessageResponse> getSessionMessages(Long sessionId, Pageable pageable);
}
