package com.carenest.backend.features.ocr.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("schema_version")
    private String schemaVersion;
    @JsonProperty("document_type")
    private String documentType;
    private Double confidence;
    private List<StructuredOcrMedicationItemDto> medications;
    private List<String> warnings;
    @JsonProperty("raw_text")
    private String rawText;
    private StructuredOcrMedicationSafetyDto safety;
}
