package com.carenest.backend.features.vaccination.dto.request;

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

    @NotNull(message = "Số thứ tự mũi tiêm không được để trống")
    @Schema(description = "Mũi thứ mấy (ví dụ: 1, 2, 99 cho mũi nhắc lại)", example = "1")
    private Integer doseNumber;

    @NotBlank(message = "Trạng thái không được để trống")
    @Schema(description = "Trạng thái mũi tiêm (COMPLETED hoặc PENDING)", example = "COMPLETED")
    private String status;

    @NotNull(message = "Ngày tiêm/dự kiến không được để trống")
    @Schema(description = "Ngày tiêm thực tế hoặc ngày hẹn dự kiến", example = "2026-05-18")
    private LocalDate date;

    @Schema(description = "Địa điểm tiêm", example = "VNVC Hoàng Văn Thụ")
    private String location;

    @Schema(description = "Ghi chú", example = "Sau khi tiêm nhớ theo dõi nhiệt độ")
    private String notes;
}
