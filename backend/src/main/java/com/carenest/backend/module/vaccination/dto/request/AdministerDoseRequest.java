package com.carenest.backend.module.vaccination.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdministerDoseRequest {

    @NotNull(message = "Ngày tiêm không được để trống")
    private LocalDate dateAdministered;

    private String location;

    private String administeredBy;

    private String notes;
}
