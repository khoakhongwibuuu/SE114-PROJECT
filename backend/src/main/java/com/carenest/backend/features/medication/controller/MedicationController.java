package com.carenest.backend.features.medication.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.core.api.PageResponse;
import com.carenest.backend.features.medication.dto.request.BatchCreateMedicationRequest;
import com.carenest.backend.features.medication.dto.request.CreateMedicationRequest;
import com.carenest.backend.features.medication.dto.request.UpdateMedicationRequest;
import com.carenest.backend.features.medication.dto.response.MedicationLogResponse;
import com.carenest.backend.features.medication.dto.response.MedicationResponse;
import com.carenest.backend.features.medication.service.MedicationService;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "Medication", description = "Quản lý đơn thuốc và lịch uống thuốc")
@SecurityRequirement(name = "bearerAuth")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
public class MedicationController {

    private final MedicationService medicationService;

    @PostMapping("/health-profiles/{profileId}/medications")
    @ResponseStatus(HttpStatus.CREATED)
    @io.swagger.v3.oas.annotations.Operation(summary = "Tạo đơn thuốc mới cho một hồ sơ sức khỏe")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo đơn thuốc thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ (thời gian, v.v.)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy hồ sơ sức khỏe")
    })
    public ApiResponse<MedicationResponse> createMedication(
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
    public ApiResponse<PageResponse<MedicationResponse>> getMedicationsByProfile(
            @PathVariable("profileId") Long profileId,
            @PageableDefault(size = 30, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<MedicationResponse> response = medicationService.getMedicationsByProfile(profileId, pageable);
        return ApiResponse.success(response);
    }

    @GetMapping("/medications/today")
    @io.swagger.v3.oas.annotations.Operation(summary = "Lấy danh sách thuốc cần uống hôm nay của một hồ sơ")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ApiResponse<List<MedicationLogResponse>> getMedicationsForToday(@RequestParam("profileId") Long profileId) {
        List<MedicationLogResponse> response = medicationService.getMedicationsForToday(profileId);
        return ApiResponse.success(response);
    }

    @PutMapping("/medications/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Cập nhật thông tin đơn thuốc (tự động tính lại lịch uống tương lai)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy đơn thuốc")
    })
    public ApiResponse<MedicationResponse> updateMedication(
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
    public ApiResponse<Void> completeMedication(@PathVariable("id") Long id) {
        medicationService.completeMedication(id);
        return ApiResponse.success("Đã kết thúc đơn thuốc", null);
    }

    @DeleteMapping("/medications/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Xóa hoàn toàn một đơn thuốc và toàn bộ lịch nhắc liên quan")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa đơn thuốc thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy đơn thuốc")
    })
    public ApiResponse<Void> deleteMedication(@PathVariable("id") Long id) {
        medicationService.deleteMedication(id);
        return ApiResponse.success("Đã xóa hoàn toàn đơn thuốc", null);
    }
}
