package com.carenest.backend.features.booking.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ConsultationMessageResponse {
    private Long id;
    private Long threadId;
    private Long senderId;
    private String senderName;
    private String senderAvatarUrl;
    private String content;
    private Instant createdAt;
}
