package com.carenest.backend.module.vaccination.dto.response;

import com.carenest.backend.module.vaccination.enums.DoseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaccinationDoseResponse {
    private Long id;
    private Integer doseNumber;
    private LocalDate scheduledDate;
    private LocalDate dateAdministered;
    private String location;
    private String administeredBy;
    private DoseStatus status;
    private String notes;
}
