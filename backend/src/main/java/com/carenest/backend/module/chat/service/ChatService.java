package com.carenest.backend.module.chat.service;

import com.carenest.backend.module.chat.dto.response.ChatMessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatService {

    /**
     * Lưu tin nhắn vào DB và trả về response đã map sang Gifted Chat format.
     */
    ChatMessageResponse saveMessage(Long familyId, Long senderId, String content);

    /**
     * Lấy lịch sử tin nhắn của gia đình theo trang — sắp xếp mới nhất đầu tiên.
     */
    Page<ChatMessageResponse> getFamilyMessages(Long familyId, Long requesterId, Pageable pageable);
}
