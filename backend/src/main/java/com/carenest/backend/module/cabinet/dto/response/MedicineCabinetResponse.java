package com.carenest.backend.module.cabinet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineCabinetResponse {

    private Long id;
    private Long familyId;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;
    
    private List<CabinetMedicineResponse> medicines;
}
