package com.carenest.backend.features.ocr.dto.response;

import com.carenest.backend.features.medication.enums.MedicationFrequency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedMedicationDto {
    @Schema(description = "Tên thuốc", example = "Paracetamol 500mg")
    private String medicineName;

    @Schema(description = "Tổng số lượng cấp phát", example = "30")
    private Integer totalQuantity;

    @Schema(description = "Đơn vị thuốc", example = "Viên")
    private String unit;

    @Schema(description = "Liều lượng uống mỗi lần", example = "1 viên/lần")
    private String dosage;

    @Schema(description = "Tần suất uống", example = "DAILY")
    private MedicationFrequency frequency;

    @Schema(description = "Số lần uống trong ngày", example = "2")
    private Integer timesPerDay;

    @Schema(description = "Số ngày uống", example = "15")
    private Integer durationDays;

    @Schema(description = "Ghi chú từ bác sĩ", example = "Uống sau khi ăn no")
    private String notes;
}
