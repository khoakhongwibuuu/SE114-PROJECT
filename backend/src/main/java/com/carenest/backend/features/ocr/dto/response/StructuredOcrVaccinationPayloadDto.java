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
public class StructuredOcrVaccinationPayloadDto {
    private String documentType;
    private Double confidence;
    private List<StructuredOcrVaccinationEntryDto> entries;
    private List<String> warnings;
    private String rawText;
}
