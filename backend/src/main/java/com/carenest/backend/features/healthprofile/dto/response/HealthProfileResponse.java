package com.carenest.backend.features.healthprofile.dto.response;

import com.carenest.backend.features.auth.enums.Gender;
import com.carenest.backend.features.healthprofile.enums.BloodType;
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
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String notes;
    private String avatarUrl;
    private Boolean isChild;
    private java.math.BigDecimal height;
    private java.math.BigDecimal weight;
    private Instant createdAt;
    private Instant updatedAt;
}
