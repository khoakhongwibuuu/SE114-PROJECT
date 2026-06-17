package com.carenest.backend.features.chat.service;

import com.carenest.backend.features.chat.dto.response.ChatMessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatService {

    /**
     * Lưu tin nhắn vào DB và trả về response đã map sang Gifted Chat format.
     */
    ChatMessageResponse saveMessage(Long familyId, Long senderId, String content);

    ChatMessageResponse saveGroupMessage(Long groupId, Long senderId, String content);

    /**
     * Lấy lịch sử tin nhắn của gia đình theo trang — sắp xếp mới nhất đầu tiên.
     */
    Page<ChatMessageResponse> getFamilyMessages(Long familyId, Long requesterId, Pageable pageable);

    Page<ChatMessageResponse> getGroupMessages(Long groupId, Long requesterId, Pageable pageable);

    void reportGroupMessage(Long messageId, Long reporterId, String reason);
}
