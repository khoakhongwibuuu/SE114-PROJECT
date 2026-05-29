package com.carenest.backend.features.chat.service;

import com.carenest.backend.features.chat.dto.response.ChatMessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatService {

    /**
     * LÆ°u tin nháº¯n vÃ o DB vÃ  tráº£ vá» response Ä‘Ã£ map sang Gifted Chat format.
     */
    ChatMessageResponse saveMessage(Long familyId, Long senderId, String content);

    /**
     * Láº¥y lá»‹ch sá»­ tin nháº¯n cá»§a gia Ä‘Ã¬nh theo trang â€” sáº¯p xáº¿p má»›i nháº¥t Ä‘áº§u tiÃªn.
     */
    Page<ChatMessageResponse> getFamilyMessages(Long familyId, Long requesterId, Pageable pageable);
}
