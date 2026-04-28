package com.carenest.backend.module.aichat.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {
    @NotBlank(message = "Nội dung chat không được để trống")
    private String message;
}
