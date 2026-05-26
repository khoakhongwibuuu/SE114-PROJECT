package com.carenest.backend.features.ocr.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ParseOcrRequest {
    @NotBlank(message = "VÄƒn báº£n thÃ´ khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String rawText;
}
