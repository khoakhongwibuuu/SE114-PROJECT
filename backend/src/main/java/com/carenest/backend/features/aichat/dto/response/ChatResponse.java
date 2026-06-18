package com.carenest.backend.features.aichat.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String reply;
    private Long id;
    @JsonAlias("message_id")
    private Long messageId;
    @JsonAlias("conversation_id")
    private Long conversationId;
    private StructuredChatPayloadResponse structured;
}
