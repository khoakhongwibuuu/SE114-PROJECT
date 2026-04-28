package com.carenest.backend.module.growth.service.impl;

import com.carenest.backend.common.exception.ResourceNotFoundException;
import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.family.entity.Family;
import com.carenest.backend.module.family.entity.FamilyMember;
import com.carenest.backend.module.family.repository.FamilyMemberRepository;
import com.carenest.backend.module.growth.dto.request.GrowthRecordCreateRequest;
import com.carenest.backend.module.growth.dto.response.GrowthChartResponse;
import com.carenest.backend.module.growth.dto.response.GrowthRecordResponse;
import com.carenest.backend.module.growth.entity.GrowthRecord;
import com.carenest.backend.module.growth.mapper.GrowthMapper;
import com.carenest.backend.module.growth.repository.GrowthRecordRepository;
import com.carenest.backend.module.growth.service.GrowthRecordService;
import com.carenest.backend.module.growth.service.WhoGrowthCalculatorService;
import com.carenest.backend.module.healthprofile.entity.HealthProfile;
import com.carenest.backend.module.healthprofile.repository.HealthProfileRepository;
import com.carenest.backend.module.notification.enums.NotificationType;
import com.carenest.backend.module.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GrowthRecordServiceImpl implements GrowthRecordService {

    private final GrowthRecordRepository growthRecordRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final GrowthMapper growthMapper;
    private final WhoGrowthCalculatorService whoGrowthCalculatorService;
    private final NotificationService notificationService;
    private final FamilyMemberRepository familyMemberRepository;

    @Override
    @Transactional
    public GrowthRecordResponse addRecord(Long profileId, GrowthRecordCreateRequest request) {
        HealthProfile profile = healthProfileRepository.findByIdAndDeletedAtIsNull(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("HealthProfile", "id", profileId.toString()));

        // Calculate BMI
        BigDecimal bmi = null;
        if (request.getWeightKg() != null && request.getHeightCm() != null && request.getHeightCm().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal heightM = request.getHeightCm().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            bmi = request.getWeightKg().divide(heightM.multiply(heightM), 1, RoundingMode.HALF_UP);
        }

        GrowthRecord record = GrowthRecord.builder()
                .healthProfile(profile)
                .recordDate(request.getRecordDate())
                .weightKg(request.getWeightKg())
                .heightCm(request.getHeightCm())
                .headCircumferenceCm(request.getHeadCircumferenceCm())
                .bmi(bmi)
                .notes(request.getNotes())
                .build();

        GrowthRecord savedRecord = growthRecordRepository.save(record);
        
        GrowthRecordResponse response = enrichWithPercentilesAndAnomalies(savedRecord, profile);
        
        if (Boolean.TRUE.equals(response.getIsAnomalous())) {
            sendAnomalyNotification(profile, savedRecord.getId(), response);
        }
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GrowthRecordResponse> getGrowthRecords(Long profileId) {
        HealthProfile profile = healthProfileRepository.findByIdAndDeletedAtIsNull(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("HealthProfile", "id", profileId.toString()));
                
        return growthRecordRepository.findByHealthProfileIdOrderByRecordDateDesc(profileId)
                .stream()
                .map(record -> enrichWithPercentilesAndAnomalies(record, profile))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GrowthChartResponse> getGrowthChartData(Long profileId) {
        HealthProfile profile = healthProfileRepository.findByIdAndDeletedAtIsNull(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("HealthProfile", "id", profileId.toString()));

        return growthRecordRepository.findByHealthProfileIdOrderByRecordDateAsc(profileId)
                .stream()
                .map(record -> {
                    GrowthRecordResponse enriched = enrichWithPercentilesAndAnomalies(record, profile);
                    return GrowthChartResponse.builder()
                            .recordDate(record.getRecordDate())
                            .weightKg(record.getWeightKg())
                            .heightCm(record.getHeightCm())
                            .bmi(record.getBmi())
                            .weightPercentile(enriched.getWeightPercentile())
                            .heightPercentile(enriched.getHeightPercentile())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private GrowthRecordResponse enrichWithPercentilesAndAnomalies(GrowthRecord record, HealthProfile profile) {
        GrowthRecordResponse response = growthMapper.toResponse(record);
        
        int ageMonths = Period.between(profile.getDateOfBirth(), record.getRecordDate()).getYears() * 12 + 
                        Period.between(profile.getDateOfBirth(), record.getRecordDate()).getMonths();
                        
        boolean isAnomalous = false;

        if (ageMonths <= 60) {
            // Weight Percentile (0-5 years)
            if (record.getWeightKg() != null) {
                WhoGrowthCalculatorService.LmsParameter wfaParam = whoGrowthCalculatorService.getWfaParameter(ageMonths, profile.getGender());
                if (wfaParam != null) {
                    double z = whoGrowthCalculatorService.calculateZScore(record.getWeightKg().doubleValue(), wfaParam);
                    double pct = whoGrowthCalculatorService.calculatePercentile(z);
                    response.setWeightPercentile(Math.round(pct * 10.0) / 10.0);
                    if (pct < 3.0 || pct > 97.0) isAnomalous = true;
                }
            }

            // Height Percentile (0-5 years)
            if (record.getHeightCm() != null) {
                WhoGrowthCalculatorService.LmsParameter lhfaParam = whoGrowthCalculatorService.getLhfaParameter(ageMonths, profile.getGender());
                if (lhfaParam != null) {
                    double z = whoGrowthCalculatorService.calculateZScore(record.getHeightCm().doubleValue(), lhfaParam);
                    double pct = whoGrowthCalculatorService.calculatePercentile(z);
                    response.setHeightPercentile(Math.round(pct * 10.0) / 10.0);
                    if (pct < 3.0 || pct > 97.0) isAnomalous = true;
                }
            }
        } else if (ageMonths >= 61 && ageMonths <= 228) {
            // BMI Percentile for 5-19 years
            if (record.getBmi() != null) {
                WhoGrowthCalculatorService.LmsParameter bmiParam = whoGrowthCalculatorService.getBmiParameter(ageMonths, profile.getGender());
                if (bmiParam != null) {
                    double z = whoGrowthCalculatorService.calculateZScore(record.getBmi().doubleValue(), bmiParam);
                    double pct = whoGrowthCalculatorService.calculatePercentile(z);
                    // For older children, we might map this to weightPercentile in the response, or we could add bmiPercentile.
                    // Since DTO has weightPercentile and heightPercentile, we'll map BMI to weightPercentile for chart consistency
                    // OR better, we just use it for anomalies and pass it to weightPercentile so the frontend sees it.
                    response.setWeightPercentile(Math.round(pct * 10.0) / 10.0);
                    if (pct < 3.0 || pct > 97.0) isAnomalous = true;
                }
            }
        }
        
        response.setIsAnomalous(isAnomalous);
        return response;
    }
    
    private void sendAnomalyNotification(HealthProfile profile, Long recordId, GrowthRecordResponse response) {
        String title = "Cảnh báo chỉ số tăng trưởng";
        StringBuilder message = new StringBuilder(String.format("Chỉ số tăng trưởng của %s cần lưu ý:", profile.getFullName()));
        
        if (response.getWeightPercentile() != null) {
            if (response.getWeightPercentile() < 3.0) message.append(" Cân nặng/BMI quá thấp (<3%).");
            else if (response.getWeightPercentile() > 97.0) message.append(" Cân nặng/BMI quá cao (>97%).");
        }
        
        if (response.getHeightPercentile() != null) {
            if (response.getHeightPercentile() < 3.0) message.append(" Chiều cao quá thấp (<3%).");
            else if (response.getHeightPercentile() > 97.0) message.append(" Chiều cao quá cao (>97%).");
        }

        Family family = profile.getFamily();
        if (family != null) {
            List<User> targetUsers = familyMemberRepository.findAllByFamilyId(family.getId())
                    .stream().map(FamilyMember::getUser).toList();
            notificationService.createNotificationForUsers(targetUsers, title, message.toString(), NotificationType.GROWTH, "GROWTH_RECORD", recordId);
        } else if (profile.getUser() != null) {
            notificationService.createNotificationForUser(profile.getUser(), title, message.toString(), NotificationType.GROWTH, "GROWTH_RECORD", recordId);
        }
    }
}
