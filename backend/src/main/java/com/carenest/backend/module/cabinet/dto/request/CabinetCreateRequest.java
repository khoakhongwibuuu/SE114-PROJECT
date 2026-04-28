package com.carenest.backend.module.cabinet.dto.request;

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

    @NotNull(message = "Family ID is required")
    private Long familyId;

    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;
}
