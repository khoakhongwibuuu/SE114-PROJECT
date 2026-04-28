package com.carenest.backend.module.ocr.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ParseOcrRequest {
    @NotBlank(message = "Văn bản thô không được để trống")
    private String rawText;
}
