package com.carenest.backend.module.vaccination.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaccinationRecordResponse {
    private Long id;
    private Long healthProfileId;
    private String vaccineName;
    private Integer totalDoses;
    private Integer doseIntervalDays;
    private String notes;
    private List<VaccinationDoseResponse> doses;
}
