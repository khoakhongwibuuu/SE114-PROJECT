package com.carenest.backend.module.healthprofile.dto.response;

import com.carenest.backend.module.auth.enums.Gender;
import com.carenest.backend.module.healthprofile.enums.BloodType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthProfileResponse {
    private Long id;
    private Long userId;
    private Long familyId;
    private String fullName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String relationship;
    private BloodType bloodType;
    private String allergies;
    private String chronicDiseases;
    private String notes;
    private String avatarUrl;
    private Boolean isChild;
    private Instant createdAt;
    private Instant updatedAt;
}
