package com.carenest.backend.features.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendConsultationMessageRequest {
    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    private String content;
}
