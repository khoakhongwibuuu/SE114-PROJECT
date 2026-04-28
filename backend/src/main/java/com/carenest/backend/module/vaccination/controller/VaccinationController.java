package com.carenest.backend.module.vaccination.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.vaccination.dto.request.AdministerDoseRequest;
import com.carenest.backend.module.vaccination.dto.request.CreateVaccinationRequest;
import com.carenest.backend.module.vaccination.dto.response.VaccinationRecordResponse;
import com.carenest.backend.module.vaccination.service.VaccinationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Vaccination", description = "Quản lý lịch tiêm chủng")
@SecurityRequirement(name = "bearerAuth")
public class VaccinationController {

    private final VaccinationService vaccinationService;

    @PostMapping("/health-profiles/{profileId}/vaccinations")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo lịch tiêm chủng mới (Tự động sinh sẵn các mũi tiêm)")
    public ApiResponse<VaccinationRecordResponse> createVaccinationPlan(
            @PathVariable Long profileId,
            @Valid @RequestBody CreateVaccinationRequest request) {
        VaccinationRecordResponse response = vaccinationService.createVaccinationPlan(profileId, request);
        return ApiResponse.success("Tạo lịch tiêm chủng thành công", response);
    }

    @GetMapping("/health-profiles/{profileId}/vaccinations")
    @Operation(summary = "Lấy lịch sử tiêm chủng của một hồ sơ")
    public ApiResponse<List<VaccinationRecordResponse>> getVaccinationHistory(@PathVariable Long profileId) {
        List<VaccinationRecordResponse> response = vaccinationService.getVaccinationHistory(profileId);
        return ApiResponse.success(response);
    }

    @PutMapping("/vaccination-doses/{doseId}/administer")
    @Operation(summary = "Ghi nhận đã tiêm một mũi (Tự động tịnh tiến ngày cho các mũi sau)")
    public ApiResponse<VaccinationRecordResponse> administerDose(
            @PathVariable Long doseId,
            @Valid @RequestBody AdministerDoseRequest request) {
        VaccinationRecordResponse response = vaccinationService.administerDose(doseId, request);
        return ApiResponse.success("Cập nhật mũi tiêm thành công", response);
    }
}
