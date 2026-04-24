package com.carenest.backend.module.vaccination.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVaccinationRequest {

    @NotBlank(message = "Tên vắc xin không được để trống")
    private String vaccineName;

    @NotNull(message = "Tổng số mũi không được để trống")
    private Integer totalDoses;

    private Integer doseIntervalDays;

    @NotNull(message = "Ngày bắt đầu (mũi 1) không được để trống")
    private LocalDate startDate;

    private String location;

    private String notes;
}
