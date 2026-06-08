package com.carenest.backend.features.cabinet.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CabinetCreateRequest {

    @NotNull(message = "Vui lòng chọn gia đình")
    private Long familyId;

    @Size(max = 100, message = "Tên tủ thuốc không được vượt quá 100 ký tự")
    private String name;
}
