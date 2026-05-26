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
        return ApiResponse.success("Láº¥y há»“ sÆ¡ sá»©c khá»e cá»§a ngÆ°á»i dÃ¹ng hiá»‡n táº¡i thÃ nh cÃ´ng", response);
    }

    @PostMapping("/health-profiles")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<HealthProfileResponse> createHealthProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody HealthProfileCreateRequest request) {
        HealthProfileResponse response = healthProfileService.createHealthProfile(user.getId(), request);
        return ApiResponse.success("Táº¡o há»“ sÆ¡ sá»©c khá»e thÃ nh cÃ´ng", response);
    }

    @GetMapping("/families/{familyId}/health-profiles")
    public ApiResponse<List<HealthProfileResponse>> getFamilyHealthProfiles(@PathVariable("familyId") Long familyId) {
        List<HealthProfileResponse> profiles = healthProfileService.getFamilyHealthProfiles(familyId);
        return ApiResponse.success("Láº¥y danh sÃ¡ch há»“ sÆ¡ sá»©c khá»e gia Ä‘Ã¬nh thÃ nh cÃ´ng", profiles);
    }

    @GetMapping("/health-profiles/{id}")
    public ApiResponse<HealthProfileResponse> getHealthProfileById(@PathVariable("id") Long id) {
        HealthProfileResponse response = healthProfileService.getHealthProfileById(id);
        return ApiResponse.success("Láº¥y chi tiáº¿t há»“ sÆ¡ sá»©c khá»e thÃ nh cÃ´ng", response);
    }

    @PutMapping("/health-profiles/{id}")
    public ApiResponse<HealthProfileResponse> updateHealthProfile(
            @PathVariable("id") Long id,
            @Valid @RequestBody HealthProfileUpdateRequest request) {
        HealthProfileResponse response = healthProfileService.updateHealthProfile(id, request);
        return ApiResponse.success("Cáº­p nháº­t há»“ sÆ¡ sá»©c khá»e thÃ nh cÃ´ng", response);
    }

    @DeleteMapping("/health-profiles/{id}")
    public ApiResponse<Void> deleteHealthProfile(@PathVariable("id") Long id) {
        healthProfileService.deleteHealthProfile(id);
        return ApiResponse.success("XÃ³a há»“ sÆ¡ sá»©c khá»e thÃ nh cÃ´ng", null);
    }

    @PutMapping("/health-profiles/{id}/medical-info")
    public ApiResponse<HealthProfileResponse> updateMedicalInfo(
            @PathVariable("id") Long id,
            @Valid @RequestBody MedicalInfoUpdateRequest request) {
        HealthProfileResponse response = healthProfileService.updateMedicalInfo(id, request);
        return ApiResponse.success("Cáº­p nháº­t thÃ´ng tin y táº¿ thÃ nh cÃ´ng", response);
    }
}
