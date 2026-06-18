package com.carenest.backend.features.ocr.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StructuredOcrMedicationSafetyDto {
    @JsonProperty("requires_confirmation")
    private boolean requiresConfirmation;
    @JsonProperty("can_save_directly")
    private boolean canSaveDirectly;
    private String disclaimer;
}
