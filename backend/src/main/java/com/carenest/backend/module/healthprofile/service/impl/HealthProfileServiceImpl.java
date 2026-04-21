package com.carenest.backend.module.healthprofile.service.impl;

import com.carenest.backend.common.exception.ResourceNotFoundException;
import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.auth.repository.UserRepository;
import com.carenest.backend.module.family.entity.Family;
import com.carenest.backend.module.family.repository.FamilyRepository;
import com.carenest.backend.module.healthprofile.dto.request.HealthProfileCreateRequest;
import com.carenest.backend.module.healthprofile.dto.request.HealthProfileUpdateRequest;
import com.carenest.backend.module.healthprofile.dto.request.MedicalInfoUpdateRequest;
import com.carenest.backend.module.healthprofile.dto.response.HealthProfileResponse;
import com.carenest.backend.module.healthprofile.entity.HealthProfile;
import com.carenest.backend.module.healthprofile.mapper.HealthProfileMapper;
import com.carenest.backend.module.healthprofile.repository.HealthProfileRepository;
import com.carenest.backend.module.healthprofile.service.HealthProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthProfileServiceImpl implements HealthProfileService {

    private final HealthProfileRepository healthProfileRepository;
    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final HealthProfileMapper healthProfileMapper;

    @Override
    @Transactional
    public HealthProfileResponse createHealthProfile(Long userId, HealthProfileCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

        Family family = null;
        if (request.getFamilyId() != null) {
            family = familyRepository.findById(request.getFamilyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Family", "id", request.getFamilyId().toString()));
        }

        HealthProfile healthProfile = HealthProfile.builder()
                .user(user)
                .family(family)
                .fullName(request.getFullName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .relationship(request.getRelationship())
                .bloodType(request.getBloodType())
                .allergies(request.getAllergies())
                .chronicDiseases(request.getChronicDiseases())
                .notes(request.getNotes())
                .avatarUrl(request.getAvatarUrl())
                .isChild(request.getIsChild() != null ? request.getIsChild() : false)
                .build();

        HealthProfile savedProfile = healthProfileRepository.save(healthProfile);
        return healthProfileMapper.toResponse(savedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HealthProfileResponse> getFamilyHealthProfiles(Long familyId) {
        if (!familyRepository.existsById(familyId)) {
            throw new ResourceNotFoundException("Family", "id", familyId.toString());
        }

        List<HealthProfile> profiles = healthProfileRepository.findByFamilyIdAndDeletedAtIsNull(familyId);
        return profiles.stream()
                .map(healthProfileMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HealthProfileResponse getHealthProfileById(Long id) {
        HealthProfile healthProfile = healthProfileRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("HealthProfile", "id", id.toString()));
        return healthProfileMapper.toResponse(healthProfile);
    }

    @Override
    @Transactional
    public HealthProfileResponse updateHealthProfile(Long id, HealthProfileUpdateRequest request) {
        HealthProfile healthProfile = healthProfileRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("HealthProfile", "id", id.toString()));

        healthProfile.setFullName(request.getFullName());
        healthProfile.setDateOfBirth(request.getDateOfBirth());
        healthProfile.setGender(request.getGender());
        healthProfile.setRelationship(request.getRelationship());
        healthProfile.setNotes(request.getNotes());
        healthProfile.setAvatarUrl(request.getAvatarUrl());
        
        if (request.getIsChild() != null) {
            healthProfile.setIsChild(request.getIsChild());
        }

        HealthProfile updatedProfile = healthProfileRepository.save(healthProfile);
        return healthProfileMapper.toResponse(updatedProfile);
    }

    @Override
    @Transactional
    public void deleteHealthProfile(Long id) {
        HealthProfile healthProfile = healthProfileRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("HealthProfile", "id", id.toString()));
        
        healthProfile.setDeletedAt(Instant.now());
        healthProfileRepository.save(healthProfile);
    }

    @Override
    @Transactional
    public HealthProfileResponse updateMedicalInfo(Long id, MedicalInfoUpdateRequest request) {
        HealthProfile healthProfile = healthProfileRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("HealthProfile", "id", id.toString()));

        healthProfile.setBloodType(request.getBloodType());
        healthProfile.setAllergies(request.getAllergies());
        healthProfile.setChronicDiseases(request.getChronicDiseases());

        HealthProfile updatedProfile = healthProfileRepository.save(healthProfile);
        return healthProfileMapper.toResponse(updatedProfile);
    }
}
