package com.carenest.backend.features.cabinet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CabinetMedicineResponse {

    private Long id;
    private String medicineName;
    private Integer quantity;
    private String unit;
    private LocalDate expiryDate;
    private LocalDate addedDate;
    private String notes;
    private boolean isExpired;
    private boolean isExpiring;
    private boolean isLowStock;
}
