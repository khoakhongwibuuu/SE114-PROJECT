package com.carenest.backend.module.growth.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrowthRecordCreateRequest {

    @NotNull(message = "Record date is required")
    private LocalDate recordDate;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "1.0", message = "Weight must be at least 1.0 kg")
    @DecimalMax(value = "150.0", message = "Weight must be at most 150.0 kg")
    private BigDecimal weightKg;

    @NotNull(message = "Height is required")
    @DecimalMin(value = "30.0", message = "Height must be at least 30.0 cm")
    @DecimalMax(value = "250.0", message = "Height must be at most 250.0 cm")
    private BigDecimal heightCm;

    @DecimalMin(value = "20.0", message = "Head circumference must be at least 20.0 cm")
    @DecimalMax(value = "70.0", message = "Head circumference must be at most 70.0 cm")
    private BigDecimal headCircumferenceCm;

    private String notes;
}
