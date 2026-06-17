package com.carenest.backend.features.healthprofile.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.healthprofile.dto.request.HealthProfileCreateRequest;
import com.carenest.backend.features.healthprofile.dto.request.HealthProfileUpdateRequest;
import com.carenest.backend.features.healthprofile.dto.request.MedicalInfoUpdateRequest;
import com.carenest.backend.features.healthprofile.dto.response.HealthProfileResponse;
import com.carenest.backend.features.healthprofile.service.HealthProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
public class HealthProfileController {

    private final HealthProfileService healthProfileService;

    @GetMapping("/health-profiles/me")
    public ApiResponse<HealthProfileResponse> getMyHealthProfile(@AuthenticationPrincipal User user) {
        HealthProfileResponse response = healthProfileService.getMyHealthProfile(user.getId());
        return ApiResponse.success("Lấy hồ sơ sức khỏe của người dùng hiện tại thành công", response);
    }

    @PostMapping("/health-profiles")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<HealthProfileResponse> createHealthProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody HealthProfileCreateRequest request) {
        HealthProfileResponse response = healthProfileService.createHealthProfile(user.getId(), request);
        return ApiResponse.success("Tạo hồ sơ sức khỏe thành công", response);
    }

    @GetMapping("/families/{familyId}/health-profiles")
    public ApiResponse<List<HealthProfileResponse>> getFamilyHealthProfiles(@PathVariable("familyId") Long familyId) {
        List<HealthProfileResponse> profiles = healthProfileService.getFamilyHealthProfiles(familyId);
        return ApiResponse.success("Lấy danh sách hồ sơ sức khỏe gia đình thành công", profiles);
    }

    @GetMapping("/health-profiles/{id}")
    public ApiResponse<HealthProfileResponse> getHealthProfileById(@PathVariable("id") Long id) {
        HealthProfileResponse response = healthProfileService.getHealthProfileById(id);
        return ApiResponse.success("Lấy chi tiết hồ sơ sức khỏe thành công", response);
    }

    @PutMapping("/health-profiles/{id}")
    public ApiResponse<HealthProfileResponse> updateHealthProfile(
            @PathVariable("id") Long id,
            @Valid @RequestBody HealthProfileUpdateRequest request) {
        HealthProfileResponse response = healthProfileService.updateHealthProfile(id, request);
        return ApiResponse.success("Cập nhật hồ sơ sức khỏe thành công", response);
    }

    @DeleteMapping("/health-profiles/{id}")
    public ApiResponse<Void> deleteHealthProfile(@PathVariable("id") Long id) {
        healthProfileService.deleteHealthProfile(id);
        return ApiResponse.success("Xóa hồ sơ sức khỏe thành công", null);
    }

    @PutMapping("/health-profiles/{id}/medical-info")
    public ApiResponse<HealthProfileResponse> updateMedicalInfo(
            @PathVariable("id") Long id,
            @Valid @RequestBody MedicalInfoUpdateRequest request) {
        HealthProfileResponse response = healthProfileService.updateMedicalInfo(id, request);
        return ApiResponse.success("Cập nhật thông tin y tế thành công", response);
    }
}
