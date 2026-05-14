package com.carenest.backend.module.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotNull(message = "familyId không được để trống")
    private Long familyId;

    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    private String content;
}
