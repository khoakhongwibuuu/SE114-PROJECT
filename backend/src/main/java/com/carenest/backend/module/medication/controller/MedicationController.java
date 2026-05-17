package com.carenest.backend.module.medication.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.medication.dto.request.BatchCreateMedicationRequest;
import com.carenest.backend.module.medication.dto.request.CreateMedicationRequest;
import com.carenest.backend.module.medication.dto.request.UpdateMedicationRequest;
import com.carenest.backend.module.medication.dto.response.MedicationLogResponse;
import com.carenest.backend.module.medication.dto.response.MedicationResponse;
import com.carenest.backend.module.medication.service.MedicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "Medication", description = "Quản lý đơn thuốc và lịch uống thuốc")
@SecurityRequirement(name = "bearerAuth")
public class MedicationController {

    private final MedicationService medicationService;

    @PostMapping("/health-profiles/{profileId}/medications")
    @ResponseStatus(HttpStatus.CREATED)
    @io.swagger.v3.oas.annotations.Operation(summary = "Tạo đơn thuốc mới cho một hồ sơ sức khỏe")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo đơn thuốc thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ (thời gian, etc)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy hồ sơ sức khỏe")
    })
    public com.carenest.backend.common.dto.ApiResponse<MedicationResponse> createMedication(
            @PathVariable("profileId") Long profileId,
            @Valid @RequestBody CreateMedicationRequest request) {
        MedicationResponse response = medicationService.createMedication(profileId, request);
        return ApiResponse.success("Tạo đơn thuốc thành công", response);
    }

    @GetMapping("/health-profiles/{profileId}/medications")
    @io.swagger.v3.oas.annotations.Operation(summary = "Lấy danh sách tất cả các đơn thuốc của một hồ sơ sức khỏe")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public com.carenest.backend.common.dto.ApiResponse<List<MedicationResponse>> getMedicationsByProfile(@PathVariable("profileId") Long profileId) {
        List<MedicationResponse> response = medicationService.getMedicationsByProfile(profileId);
        return ApiResponse.success(response);
    }

    @GetMapping("/medications/today")
    @io.swagger.v3.oas.annotations.Operation(summary = "Lấy danh sách thuốc cần uống HÔM NAY của một hồ sơ")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public com.carenest.backend.common.dto.ApiResponse<List<MedicationLogResponse>> getMedicationsForToday(@RequestParam("profileId") Long profileId) {
        List<MedicationLogResponse> response = medicationService.getMedicationsForToday(profileId);
        return ApiResponse.success(response);
    }

    @PutMapping("/medications/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Cập nhật thông tự động tính lại lịch uống tương lai)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy đơn thuốc")
    })
    public com.carenest.backend.common.dto.ApiResponse<MedicationResponse> updateMedication(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateMedicationRequest request) {
        MedicationResponse response = medicationService.updateMedication(id, request);
        return ApiResponse.success("Cập nhật đơn thuốc thành công", response);
    }

    @PutMapping("/medications/{id}/complete")
    @io.swagger.v3.oas.annotations.Operation(summary = "Đánh dấu kết thúc sớm một đơn thuốc")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Kết thúc đơn thuốc thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy đơn thuốc")
    })
    public com.carenest.backend.common.dto.ApiResponse<Void> completeMedication(@PathVariable("id") Long id) {
        medicationService.completeMedication(id);
        return ApiResponse.success("Đã kết thúc đơn thuốc", null);
    }
}
