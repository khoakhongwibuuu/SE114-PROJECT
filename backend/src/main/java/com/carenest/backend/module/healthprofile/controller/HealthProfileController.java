package com.carenest.backend.module.healthprofile.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.healthprofile.dto.request.HealthProfileCreateRequest;
import com.carenest.backend.module.healthprofile.dto.request.HealthProfileUpdateRequest;
import com.carenest.backend.module.healthprofile.dto.request.MedicalInfoUpdateRequest;
import com.carenest.backend.module.healthprofile.dto.response.HealthProfileResponse;
import com.carenest.backend.module.healthprofile.service.HealthProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class HealthProfileController {

    private final HealthProfileService healthProfileService;

    @PostMapping("/health-profiles")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<HealthProfileResponse> createHealthProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody HealthProfileCreateRequest request) {
        HealthProfileResponse response = healthProfileService.createHealthProfile(user.getId(), request);
        return ApiResponse.success("Created health profile successfully", response);
    }

    @GetMapping("/families/{familyId}/health-profiles")
    public ApiResponse<List<HealthProfileResponse>> getFamilyHealthProfiles(@PathVariable Long familyId) {
        List<HealthProfileResponse> profiles = healthProfileService.getFamilyHealthProfiles(familyId);
        return ApiResponse.success("Fetched family health profiles", profiles);
    }

    @GetMapping("/health-profiles/{id}")
    public ApiResponse<HealthProfileResponse> getHealthProfileById(@PathVariable Long id) {
        HealthProfileResponse response = healthProfileService.getHealthProfileById(id);
        return ApiResponse.success("Fetched health profile details", response);
    }

    @PutMapping("/health-profiles/{id}")
    public ApiResponse<HealthProfileResponse> updateHealthProfile(
            @PathVariable Long id,
            @Valid @RequestBody HealthProfileUpdateRequest request) {
        HealthProfileResponse response = healthProfileService.updateHealthProfile(id, request);
        return ApiResponse.success("Updated health profile successfully", response);
    }

    @DeleteMapping("/health-profiles/{id}")
    public ApiResponse<Void> deleteHealthProfile(@PathVariable Long id) {
        healthProfileService.deleteHealthProfile(id);
        return ApiResponse.success("Deleted health profile successfully", null);
    }

    @PutMapping("/health-profiles/{id}/medical-info")
    public ApiResponse<HealthProfileResponse> updateMedicalInfo(
            @PathVariable Long id,
            @Valid @RequestBody MedicalInfoUpdateRequest request) {
        HealthProfileResponse response = healthProfileService.updateMedicalInfo(id, request);
        return ApiResponse.success("Updated medical info successfully", response);
    }
}
