package com.carenest.backend.module.medication.dto.request;

import com.carenest.backend.module.medication.enums.MedicationFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateMedicationRequest {

    @NotBlank(message = "Tên thuốc không được để trống")
    private String medicineName;

    private String dosage;

    @NotNull(message = "Tần suất uống thuốc không được để trống")
    private MedicationFrequency frequency;

    private Integer timesPerDay;

    // Danh sách giờ uống thuốc, VD: ["08:00", "13:00", "20:00"]
    private List<String> timeSlots;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    private LocalDate endDate;

    private String notes;
}
