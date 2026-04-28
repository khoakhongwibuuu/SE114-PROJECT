package com.carenest.backend.module.vaccination.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVaccinationRequest {

    @NotBlank(message = "Tên vắc xin không được để trống")
    @Schema(description = "Tên vắc xin", example = "Vắc-xin 6 trong 1 Hexaxim")
    private String vaccineName;

    @NotNull(message = "Tổng số mũi không được để trống")
    @Schema(description = "Tổng số mũi cần tiêm", example = "3")
    private Integer totalDoses;

    @Schema(description = "Khoảng cách giữa các mũi (số ngày)", example = "30")
    private Integer doseIntervalDays;

    @NotNull(message = "Ngày bắt đầu (mũi 1) không được để trống")
    @Schema(description = "Ngày tiêm mũi đầu tiên", example = "2023-11-01")
    private LocalDate startDate;

    @Schema(description = "Địa điểm tiêm", example = "VNVC Hoàng Văn Thụ")
    private String location;

    @Schema(description = "Ghi chú", example = "Sau khi tiêm nhớ theo dõi nhiệt độ")
    private String notes;
}
