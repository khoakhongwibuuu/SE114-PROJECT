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
public class StructuredOcrMedicationItemDto {
    private String name;
    private String strength;
    private String form;
    @JsonProperty("dose_instruction")
    private String doseInstruction;
    private String frequency;
    @JsonProperty("duration_days")
    private Integer durationDays;
    @JsonProperty("total_quantity")
    private Integer totalQuantity;
    private String route;
    private Double confidence;
    private List<String> warnings;
}
