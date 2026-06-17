package com.carenest.backend.features.healthprofile.service.impl;

import com.carenest.backend.core.exception.ResourceNotFoundException;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.family.entity.Family;
import com.carenest.backend.features.family.repository.FamilyRepository;
import com.carenest.backend.features.family.util.FamilySecurityUtil;
import com.carenest.backend.features.healthprofile.dto.request.HealthProfileCreateRequest;
import com.carenest.backend.features.healthprofile.dto.request.HealthProfileUpdateRequest;
import com.carenest.backend.features.healthprofile.dto.request.MedicalInfoUpdateRequest;
import com.carenest.backend.features.healthprofile.dto.response.HealthProfileResponse;
import com.carenest.backend.features.healthprofile.entity.HealthProfile;
import com.carenest.backend.features.healthprofile.mapper.HealthProfileMapper;
import com.carenest.backend.features.healthprofile.repository.HealthProfileRepository;
import com.carenest.backend.features.healthprofile.service.HealthProfileService;
import com.carenest.backend.features.growth.repository.GrowthRecordRepository;
import com.carenest.backend.features.growth.entity.GrowthRecord;
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
    private final GrowthRecordRepository growthRecordRepository;
    private final FamilySecurityUtil familySecurityUtil;

    @Override
    @Transactional
    public HealthProfileResponse createHealthProfile(Long userId, HealthProfileCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

        Family family = null;
        if (request.getFamilyId() != null) {
            familySecurityUtil.checkUserBelongsToFamily(request.getFamilyId());
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
        return enrichWithHeightAndWeight(healthProfileMapper.toResponse(savedProfile));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HealthProfileResponse> getFamilyHealthProfiles(Long familyId) {
        familySecurityUtil.checkUserBelongsToFamily(familyId);

        if (!familyRepository.existsById(familyId)) {
            throw new ResourceNotFoundException("Family", "id", familyId.toString());
        }

        List<HealthProfile> profiles = healthProfileRepository.findByFamilyIdAndDeletedAtIsNull(familyId);
        return profiles.stream()
                .map(healthProfileMapper::toResponse)
                .map(this::enrichWithHeightAndWeight)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HealthProfileResponse getHealthProfileById(Long id) {
        familySecurityUtil.checkUserBelongsToHealthProfile(id);

        HealthProfile healthProfile = healthProfileRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("HealthProfile", "id", id.toString()));
        return enrichWithHeightAndWeight(healthProfileMapper.toResponse(healthProfile));
    }

    @Override
    @Transactional
    public HealthProfileResponse updateHealthProfile(Long id, HealthProfileUpdateRequest request) {
        familySecurityUtil.checkUserBelongsToHealthProfile(id);

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
        saveHeightAndWeight(updatedProfile, request.getHeight(), request.getWeight());
        return enrichWithHeightAndWeight(healthProfileMapper.toResponse(updatedProfile));
    }

    @Override
    @Transactional
    public void deleteHealthProfile(Long id) {
        familySecurityUtil.checkUserBelongsToHealthProfile(id);

        HealthProfile healthProfile = healthProfileRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("HealthProfile", "id", id.toString()));

        healthProfile.setDeletedAt(Instant.now());
        healthProfileRepository.save(healthProfile);
    }

    @Override
    @Transactional
    public HealthProfileResponse updateMedicalInfo(Long id, MedicalInfoUpdateRequest request) {
        familySecurityUtil.checkUserBelongsToHealthProfile(id);

        HealthProfile healthProfile = healthProfileRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("HealthProfile", "id", id.toString()));

        healthProfile.setBloodType(request.getBloodType());
        healthProfile.setAllergies(request.getAllergies());
        healthProfile.setChronicDiseases(request.getChronicDiseases());

        HealthProfile updatedProfile = healthProfileRepository.save(healthProfile);
        return enrichWithHeightAndWeight(healthProfileMapper.toResponse(updatedProfile));
    }

    @Override
    @Transactional
    public HealthProfileResponse getMyHealthProfile(Long userId) {
        List<HealthProfile> profiles = healthProfileRepository.findByUserIdAndDeletedAtIsNull(userId);
        if (profiles.isEmpty()) {
            // Auto-create profile if missing
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId));

            HealthProfile newProfile = HealthProfile.builder()
                    .user(user)
                    .fullName(user.getFullName())
                    .dateOfBirth(user.getDateOfBirth() != null ? user.getDateOfBirth() : java.time.LocalDate.of(2000, 1, 1))
                    .gender(user.getGender() != null ? user.getGender() : com.carenest.backend.features.auth.enums.Gender.OTHER)
                    .isChild(false)
                    .build();

            HealthProfile saved = healthProfileRepository.save(newProfile);
            return enrichWithHeightAndWeight(healthProfileMapper.toResponse(saved));
        }
        // Usually the first one is the main profile
        return enrichWithHeightAndWeight(healthProfileMapper.toResponse(profiles.get(0)));
    }

    private HealthProfileResponse enrichWithHeightAndWeight(HealthProfileResponse response) {
        if (response == null) return null;
        List<GrowthRecord> growthRecords = growthRecordRepository.findByHealthProfileIdOrderByRecordDateDesc(response.getId());
        if (!growthRecords.isEmpty()) {
            GrowthRecord latest = growthRecords.get(0);
            response.setHeight(latest.getHeightCm());
            response.setWeight(latest.getWeightKg());
        }
        return response;
    }

    private void saveHeightAndWeight(HealthProfile profile, java.math.BigDecimal height, java.math.BigDecimal weight) {
        if (height == null && weight == null) return;

        java.time.LocalDate today = java.time.LocalDate.now();
        List<GrowthRecord> records = growthRecordRepository.findByHealthProfileIdOrderByRecordDateDesc(profile.getId());

        GrowthRecord record = null;
        // Check if there is already a record for today
        for (GrowthRecord r : records) {
            if (r.getRecordDate().equals(today)) {
                record = r;
                break;
            }
        }

        if (record == null) {
            // Find latest values to fill missing fields if only one is updated
            java.math.BigDecimal lastHeight = height;
            java.math.BigDecimal lastWeight = weight;
            if (!records.isEmpty()) {
                GrowthRecord latest = records.get(0);
                if (lastHeight == null) lastHeight = latest.getHeightCm();
                if (lastWeight == null) lastWeight = latest.getWeightKg();
            }

            // Set defaults if still null to avoid validation errors
            if (lastHeight == null) lastHeight = new java.math.BigDecimal("160.0");
            if (lastWeight == null) lastWeight = new java.math.BigDecimal("55.0");

            java.math.BigDecimal bmi = null;
            if (lastHeight.compareTo(java.math.BigDecimal.ZERO) > 0) {
                java.math.BigDecimal heightM = lastHeight.divide(new java.math.BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP);
                bmi = lastWeight.divide(heightM.multiply(heightM), 1, java.math.RoundingMode.HALF_UP);
            }

            record = GrowthRecord.builder()
                    .healthProfile(profile)
                    .recordDate(today)
                    .heightCm(lastHeight)
                    .weightKg(lastWeight)
                    .bmi(bmi)
                    .notes("Cập nhật từ hồ sơ")
                    .build();
        } else {
            if (height != null) record.setHeightCm(height);
            if (weight != null) record.setWeightKg(weight);

            java.math.BigDecimal currentHeight = record.getHeightCm();
            java.math.BigDecimal currentWeight = record.getWeightKg();
            if (currentHeight != null && currentWeight != null && currentHeight.compareTo(java.math.BigDecimal.ZERO) > 0) {
                java.math.BigDecimal heightM = currentHeight.divide(new java.math.BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP);
                record.setBmi(currentWeight.divide(heightM.multiply(heightM), 1, java.math.RoundingMode.HALF_UP));
            }
        }

        growthRecordRepository.save(record);
    }
}
