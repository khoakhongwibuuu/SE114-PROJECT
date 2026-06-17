package com.carenest.backend.features.growth.dto.request;

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

    @NotNull(message = "Vui lòng nhập ngày ghi nhận")
    private LocalDate recordDate;

    @NotNull(message = "Vui lòng nhập cân nặng")
    @DecimalMin(value = "1.0", message = "Cân nặng phải tối thiểu 1.0 kg")
    @DecimalMax(value = "150.0", message = "Cân nặng không được vượt quá 150.0 kg")
    private BigDecimal weightKg;

    @NotNull(message = "Vui lòng nhập chiều cao")
    @DecimalMin(value = "30.0", message = "Chiều cao phải tối thiểu 30.0 cm")
    @DecimalMax(value = "250.0", message = "Chiều cao không được vượt quá 250.0 cm")
    private BigDecimal heightCm;

    @DecimalMin(value = "20.0", message = "Vòng đầu phải tối thiểu 20.0 cm")
    @DecimalMax(value = "70.0", message = "Vòng đầu không được vượt quá 70.0 cm")
    private BigDecimal headCircumferenceCm;

    private String notes;
}
