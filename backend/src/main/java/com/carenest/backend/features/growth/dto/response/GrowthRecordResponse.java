package com.carenest.backend.features.growth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrowthRecordResponse {

    private Long id;
    private LocalDate recordDate;
    private BigDecimal weightKg;
    private BigDecimal heightCm;
    private BigDecimal headCircumferenceCm;
    private BigDecimal bmi;
    private Double weightPercentile;
    private Double heightPercentile;
    private Boolean isAnomalous;
    private String notes;
    private Instant createdAt;
}
