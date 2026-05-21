package com.carenest.backend.module.cabinet.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
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
public class CabinetMedicineUpdateRequest {

    @Size(max = 200, message = "Tên thuốc không được vượt quá 200 ký tự")
    private String medicineName;

    @Min(value = 0, message = "Số lượng không được âm")
    private Integer quantity;

    @Size(max = 50, message = "Đơn vị không được vượt quá 50 ký tự")
    private String unit;

    @FutureOrPresent(message = "Ngày hết hạn không được nằm trong quá khứ")
    private LocalDate expiryDate;

    private String notes;
}
