package com.carenest.backend.features.aichat.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {
    @NotBlank(message = "Ná»™i dung chat khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String message;
}
