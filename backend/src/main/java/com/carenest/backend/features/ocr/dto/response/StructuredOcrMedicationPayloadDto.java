package com.carenest.backend.features.ocr.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StructuredOcrMedicationPayloadDto {
    private String documentType;
    private Double confidence;
    private List<StructuredOcrMedicationItemDto> medications;
    private List<String> warnings;
    private String rawText;
}
