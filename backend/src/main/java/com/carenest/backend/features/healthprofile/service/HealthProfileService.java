package com.carenest.backend.features.healthprofile.service;

import com.carenest.backend.features.healthprofile.dto.request.HealthProfileCreateRequest;
import com.carenest.backend.features.healthprofile.dto.request.HealthProfileUpdateRequest;
import com.carenest.backend.features.healthprofile.dto.request.MedicalInfoUpdateRequest;
import com.carenest.backend.features.healthprofile.dto.response.HealthProfileResponse;

import java.util.List;

public interface HealthProfileService {
    HealthProfileResponse createHealthProfile(Long userId, HealthProfileCreateRequest request);
    List<HealthProfileResponse> getFamilyHealthProfiles(Long familyId);
    HealthProfileResponse getHealthProfileById(Long id);
    HealthProfileResponse updateHealthProfile(Long id, HealthProfileUpdateRequest request);
    void deleteHealthProfile(Long id);
    HealthProfileResponse updateMedicalInfo(Long id, MedicalInfoUpdateRequest request);
    HealthProfileResponse getMyHealthProfile(Long userId);
}
