package com.carenest.backend.module.family.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateFamilyRequest {

    @NotBlank(message = "Tên gia đình không được để trống")
    @Size(min = 2, max = 100, message = "Tên gia đình phải từ 2 đến 100 ký tự")
    private String name;
}
