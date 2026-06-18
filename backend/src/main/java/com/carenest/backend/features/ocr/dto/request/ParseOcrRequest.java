package com.carenest.backend.features.ocr.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ParseOcrRequest {
    @NotBlank(message = "Văn bản OCR không được để trống")
    private String rawText;
}
