package com.carenest.backend.module.aichat.service.impl;

import com.carenest.backend.module.aichat.dto.request.ChatRequest;
import com.carenest.backend.module.aichat.dto.response.ChatResponse;
import com.carenest.backend.module.aichat.service.AiChatService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class MockAiChatServiceImpl implements AiChatService {

    @Override
    public ChatResponse sendMessage(ChatRequest request) {
        // [QUY TẮC 1]: Profile DEV trả về câu trả lời giả lập. Không tốn API token.
        return ChatResponse.builder()
                .reply("Xin chào! Tôi là trợ lý ảo CareNest (Chế độ Mock). " +
                       "Câu hỏi của bạn là: '" + request.getMessage() + "'. " +
                       "Tôi hiện đang trong môi trường DEV nên không thể kết nối tới AI thật. " +
                       "Vui lòng đi khám bác sĩ nếu có triệu chứng nặng nhé!")
                .build();
    }
}
