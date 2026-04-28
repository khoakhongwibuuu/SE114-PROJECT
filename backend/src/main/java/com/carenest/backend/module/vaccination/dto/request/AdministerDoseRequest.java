package com.carenest.backend.module.vaccination.dto.request;

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
public class AdministerDoseRequest {

    @NotNull(message = "Ngày tiêm không được để trống")
    @Schema(description = "Ngày thực tế đã tiêm", example = "2023-11-01")
    private LocalDate dateAdministered;

    @Schema(description = "Địa điểm tiêm", example = "VNVC Hoàng Văn Thụ")
    private String location;

    @Schema(description = "Người thực hiện tiêm", example = "BS. Nguyễn Văn A")
    private String administeredBy;

    @Schema(description = "Ghi chú sau tiêm", example = "Bé khóc nhiều, không sốt")
    private String notes;
}
