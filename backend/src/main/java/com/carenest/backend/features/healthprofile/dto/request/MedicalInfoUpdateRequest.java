package com.carenest.backend.features.healthprofile.dto.request;

import com.carenest.backend.features.healthprofile.enums.BloodType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalInfoUpdateRequest {
    private BloodType bloodType;
    private String allergies;
    private String chronicDiseases;
}
