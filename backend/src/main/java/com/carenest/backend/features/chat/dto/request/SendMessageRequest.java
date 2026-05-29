package com.carenest.backend.features.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotNull(message = "familyId khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Long familyId;

    @NotBlank(message = "Ná»™i dung tin nháº¯n khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String content;
}
