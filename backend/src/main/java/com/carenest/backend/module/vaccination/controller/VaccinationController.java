package com.carenest.backend.module.vaccination.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.vaccination.dto.request.AdministerDoseRequest;
import com.carenest.backend.module.vaccination.dto.request.CreateVaccinationRequest;
import com.carenest.backend.module.vaccination.dto.response.VaccinationRecordResponse;
import com.carenest.backend.module.vaccination.service.VaccinationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
    @io.swagger.v3.oas.annotations.Operation(summary = "Tạo lịch tiêm chủng mới (Tự động sinh sẵn các mũi tiêm)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo lịch tiêm chủng thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy hồ sơ sức khỏe")
    })
    public com.carenest.backend.common.dto.ApiResponse<VaccinationRecordResponse> createVaccinationPlan(
            @PathVariable Long profileId,
            @Valid @RequestBody CreateVaccinationRequest request) {
        VaccinationRecordResponse response = vaccinationService.createVaccinationPlan(profileId, request);
        return ApiResponse.success("Tạo lịch tiêm chủng thành công", response);
    }

    @GetMapping("/health-profiles/{profileId}/vaccinations")
    @io.swagger.v3.oas.annotations.Operation(summary = "Lấy lịch sử tiêm chủng của một hồ sơ")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy lịch sử thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy hồ sơ sức khỏe")
    })
    public com.carenest.backend.common.dto.ApiResponse<List<VaccinationRecordResponse>> getVaccinationHistory(@PathVariable Long profileId) {
        List<VaccinationRecordResponse> response = vaccinationService.getVaccinationHistory(profileId);
        return ApiResponse.success(response);
    }

    @PutMapping("/vaccination-doses/{doseId}/administer")
    @io.swagger.v3.oas.annotations.Operation(summary = "Ghi nhận đã tiêm một mũi (Tự động tịnh tiến ngày cho các mũi sau)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Mũi tiêm đã được hoàn thành trước đó"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy mũi tiêm")
    })
    public com.carenest.backend.common.dto.ApiResponse<VaccinationRecordResponse> administerDose(
            @PathVariable Long doseId,
            @Valid @RequestBody AdministerDoseRequest request) {
        VaccinationRecordResponse response = vaccinationService.administerDose(doseId, request);
        return ApiResponse.success("Cập nhật mũi tiêm thành công", response);
    }
}
