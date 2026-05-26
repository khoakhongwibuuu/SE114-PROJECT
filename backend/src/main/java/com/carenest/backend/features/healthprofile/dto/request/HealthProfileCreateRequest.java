package com.carenest.backend.features.healthprofile.dto.request;

import com.carenest.backend.features.auth.enums.Gender;
import com.carenest.backend.features.healthprofile.enums.BloodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
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
public class HealthProfileCreateRequest {

    private Long familyId; // Optional

    @NotBlank(message = "Vui lÃ²ng nháº­p há» vÃ  tÃªn")
    @Size(max = 100, message = "Há» vÃ  tÃªn khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 100 kÃ½ tá»±")
    private String fullName;

    @NotNull(message = "Vui lÃ²ng nháº­p ngÃ y sinh")
    @PastOrPresent(message = "NgÃ y sinh khÃ´ng Ä‘Æ°á»£c náº±m trong tÆ°Æ¡ng lai")
    private LocalDate dateOfBirth;

    @NotNull(message = "Vui lÃ²ng chá»n giá»›i tÃ­nh")
    private Gender gender;

    @Size(max = 50, message = "Quan há»‡ khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 50 kÃ½ tá»±")
    private String relationship;

    private BloodType bloodType;

    private String allergies;

    private String chronicDiseases;

    private String notes;

    @Size(max = 500, message = "ÄÆ°á»ng dáº«n áº£nh Ä‘áº¡i diá»‡n khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 500 kÃ½ tá»±")
    private String avatarUrl;

    @Builder.Default
    private Boolean isChild = false;
}
