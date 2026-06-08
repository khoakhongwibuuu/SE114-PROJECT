package com.carenest.backend.features.cabinet.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CabinetMedicineRequest {

    @NotBlank(message = "Vui lòng nhập tên thuốc")
    @Size(max = 200, message = "Tên thuốc không được vượt quá 200 ký tự")
    private String medicineName;

    @Min(value = 1, message = "Số lượng phải lớn hơn hoặc bằng 1")
    private Integer quantity;

    @Size(max = 50, message = "Đơn vị không được vượt quá 50 ký tự")
    private String unit;

    @FutureOrPresent(message = "Ngày hết hạn không được nằm trong quá khứ")
    private LocalDate expiryDate;

    private String notes;
}
