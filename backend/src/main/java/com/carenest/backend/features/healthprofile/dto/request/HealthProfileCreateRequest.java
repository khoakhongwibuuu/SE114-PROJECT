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

    @NotBlank(message = "Vui lòng nhập họ và tên")
    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    private String fullName;

    @NotNull(message = "Vui lòng nhập ngày sinh")
    @PastOrPresent(message = "Ngày sinh không được nằm trong tương lai")
    private LocalDate dateOfBirth;

    @NotNull(message = "Vui lòng chọn giới tính")
    private Gender gender;

    @Size(max = 50, message = "Quan hệ không được vượt quá 50 ký tự")
    private String relationship;

    private BloodType bloodType;

    private String allergies;

    private String chronicDiseases;

    private String notes;

    @Size(max = 500, message = "Đường dẫn ảnh đại diện không được vượt quá 500 ký tự")
    private String avatarUrl;

    @Builder.Default
    private Boolean isChild = false;
}
