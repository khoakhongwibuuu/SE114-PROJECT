package com.carenest.backend.module.medication.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.medication.dto.request.BatchCreateMedicationRequest;
import com.carenest.backend.module.medication.dto.request.CreateMedicationRequest;
import com.carenest.backend.module.medication.dto.request.UpdateMedicationRequest;
import com.carenest.backend.module.medication.dto.response.MedicationLogResponse;
import com.carenest.backend.module.medication.dto.response.MedicationResponse;
import com.carenest.backend.module.medication.service.MedicationService;
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
@Tag(name = "Medication", description = "Quản lý đơn thuốc và lịch uống thuốc")
@SecurityRequirement(name = "bearerAuth")
public class MedicationController {

    private final MedicationService medicationService;

    @PostMapping("/health-profiles/{profileId}/medications")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo đơn thuốc mới cho một hồ sơ sức khỏe")
    public ApiResponse<MedicationResponse> createMedication(
            @PathVariable Long profileId,
            @Valid @RequestBody CreateMedicationRequest request) {
        MedicationResponse response = medicationService.createMedication(profileId, request);
        return ApiResponse.success("Tạo đơn thuốc thành công", response);
    }

    @GetMapping("/health-profiles/{profileId}/medications")
    @Operation(summary = "Lấy danh sách tất cả các đơn thuốc của một hồ sơ sức khỏe")
    public ApiResponse<List<MedicationResponse>> getMedicationsByProfile(@PathVariable Long profileId) {
        List<MedicationResponse> response = medicationService.getMedicationsByProfile(profileId);
        return ApiResponse.success(response);
    }

    @GetMapping("/medications/today")
    @Operation(summary = "Lấy danh sách thuốc cần uống HÔM NAY của một hồ sơ")
    public ApiResponse<List<MedicationLogResponse>> getMedicationsForToday(@RequestParam Long profileId) {
        List<MedicationLogResponse> response = medicationService.getMedicationsForToday(profileId);
        return ApiResponse.success(response);
    }

    @PutMapping("/medications/{id}")
    @Operation(summary = "Cập nhật thông tin đơn thuốc (Tự động tính lại lịch uống tương lai)")
    public ApiResponse<MedicationResponse> updateMedication(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMedicationRequest request) {
        MedicationResponse response = medicationService.updateMedication(id, request);
        return ApiResponse.success("Cập nhật đơn thuốc thành công", response);
    }

    @PutMapping("/medications/{id}/complete")
    @Operation(summary = "Đánh dấu kết thúc sớm một đơn thuốc")
    public ApiResponse<Void> completeMedication(@PathVariable Long id) {
        medicationService.completeMedication(id);
        return ApiResponse.success("Đã kết thúc đơn thuốc", null);
    }
}
