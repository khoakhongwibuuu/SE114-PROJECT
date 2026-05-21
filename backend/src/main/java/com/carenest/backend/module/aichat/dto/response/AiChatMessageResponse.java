package com.carenest.backend.module.aichat.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AiChatMessageResponse {
    private Long id;
    private Long sessionId;
    private String role;
    private String content;
    private Instant createdAt;
}
