package com.carenest.backend.features.vaccination.service.impl;

import com.carenest.backend.core.exception.BadRequestException;
import com.carenest.backend.core.exception.ResourceNotFoundException;
import com.carenest.backend.features.family.util.FamilySecurityUtil;
import com.carenest.backend.features.healthprofile.entity.HealthProfile;
import com.carenest.backend.features.healthprofile.repository.HealthProfileRepository;
import com.carenest.backend.features.vaccination.dto.request.AdministerDoseRequest;
import com.carenest.backend.features.vaccination.dto.request.CreateVaccinationRequest;
import com.carenest.backend.features.vaccination.dto.response.VaccinationRecordResponse;
import com.carenest.backend.features.vaccination.entity.VaccinationDose;
import com.carenest.backend.features.vaccination.entity.VaccinationRecord;
import com.carenest.backend.features.vaccination.enums.DoseStatus;
import com.carenest.backend.features.vaccination.mapper.VaccinationMapper;
import com.carenest.backend.features.vaccination.repository.VaccinationDoseRepository;
import com.carenest.backend.features.vaccination.repository.VaccinationRecordRepository;
import com.carenest.backend.features.vaccination.service.VaccinationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.CacheManager;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaccinationServiceImpl implements VaccinationService {

    private final VaccinationRecordRepository vaccinationRecordRepository;
    private final VaccinationDoseRepository vaccinationDoseRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final VaccinationMapper vaccinationMapper;
    private final FamilySecurityUtil familySecurityUtil;
    private final CacheManager cacheManager;

    private void evictDashboardCache(HealthProfile profile) {
        if (profile != null && cacheManager.getCache("dashboard") != null) {
            for (Long familyId : familySecurityUtil.getFamilyIdsForProfile(profile)) {
                cacheManager.getCache("dashboard").evict(familyId);
            }
        }
    }

    @Override
    @Transactional
    public VaccinationRecordResponse createVaccinationPlan(Long profileId, CreateVaccinationRequest request) {
        familySecurityUtil.checkUserBelongsToHealthProfile(profileId);

        HealthProfile profile = healthProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("HealthProfile", profileId));

        // 1. TÃƒÂ¬m hoÃ¡ÂºÂ·c tÃ¡ÂºÂ¡o mÃ¡Â»â€ºi VaccinationRecord theo vaccineName (khÃƒÂ´ng phÃƒÂ¢n biÃ¡Â»â€¡t hoa thÃ†Â°Ã¡Â»Âng)
        VaccinationRecord record = vaccinationRecordRepository
                .findByHealthProfileIdAndVaccineNameIgnoreCase(profileId, request.getVaccineName())
                .orElseGet(() -> {
                    VaccinationRecord newRecord = VaccinationRecord.builder()
                            .healthProfile(profile)
                            .vaccineName(request.getVaccineName())
                            .totalDoses(1)
                            .notes(request.getNotes())
                            .build();
                    return vaccinationRecordRepository.save(newRecord);
                });

        // CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t lÃ¡ÂºÂ¡i totalDoses trong record nÃ¡ÂºÂ¿u doseNumber lÃ¡Â»â€ºn hÃ†Â¡n totalDoses hiÃ¡Â»â€¡n tÃ¡ÂºÂ¡i
        if (request.getDoseNumber() > record.getTotalDoses()) {
            record.setTotalDoses(request.getDoseNumber());
            vaccinationRecordRepository.save(record);
        }

        // 2. TÃƒÂ¬m hoÃ¡ÂºÂ·c tÃ¡ÂºÂ¡o mÃ¡Â»â€ºi VaccinationDose cho doseNumber cÃ¡Â»Â§a record nÃƒÂ y
        DoseStatus targetStatus = DoseStatus.valueOf(request.getStatus().toUpperCase());
        LocalDate dateAdministered = (targetStatus == DoseStatus.COMPLETED) ? request.getDate() : null;
        LocalDate scheduledDate = request.getDate();

        VaccinationDose dose = vaccinationDoseRepository
                .findByVaccinationRecordIdAndDoseNumber(record.getId(), request.getDoseNumber())
                .orElseGet(() -> VaccinationDose.builder()
                        .vaccinationRecord(record)
                        .doseNumber(request.getDoseNumber())
                        .build());

        dose.setScheduledDate(scheduledDate);
        dose.setDateAdministered(dateAdministered);
        dose.setStatus(targetStatus);
        if (request.getLocation() != null) dose.setLocation(request.getLocation());
        if (request.getNotes() != null) dose.setNotes(request.getNotes());

        vaccinationDoseRepository.save(dose);

        evictDashboardCache(profile);

        // 3. TrÃ¡ÂºÂ£ vÃ¡Â»Â toÃƒÂ n bÃ¡Â»â„¢ cÃƒÂ¡c mÃ…Â©i tiÃƒÂªm thuÃ¡Â»â„¢c record nÃƒÂ y Ã„â€˜Ã¡Â»Æ’ Ã„â€˜Ã¡Â»â€œng bÃ¡Â»â„¢ hÃƒÂ³a danh sÃƒÂ¡ch UI
        List<VaccinationDose> doses = vaccinationDoseRepository.findAllByVaccinationRecordIdOrderByDoseNumberAsc(record.getId());
        return vaccinationMapper.toRecordResponseWithDoses(record, doses);
    }

    @Override
    @Transactional
    public VaccinationRecordResponse administerDose(Long doseId, AdministerDoseRequest request) {
        VaccinationDose currentDose = vaccinationDoseRepository.findById(doseId)
                .orElseThrow(() -> new ResourceNotFoundException("VaccinationDose", doseId));

        VaccinationRecord record = currentDose.getVaccinationRecord();
        familySecurityUtil.checkUserBelongsToHealthProfile(record.getHealthProfile().getId());

        if (currentDose.getStatus() == DoseStatus.COMPLETED) {
            throw new BadRequestException("Mũi tiêm này đã được hoàn thành trước đó.");
        }

        // Cập nhật thông tin tiêm chủng cho mũi hiện tại
        currentDose.setDateAdministered(request.getDateAdministered());
        currentDose.setStatus(DoseStatus.COMPLETED);
        if (request.getLocation() != null) currentDose.setLocation(request.getLocation());
        if (request.getAdministeredBy() != null) currentDose.setAdministeredBy(request.getAdministeredBy());
        if (request.getNotes() != null) currentDose.setNotes(request.getNotes());

        vaccinationDoseRepository.save(currentDose);

        // Logic tịnh tiến ngày tiêm (Auto-Rescheduling)
        // Yêu cầu từ Tech Lead: cập nhật lại scheduledDate cho các mũi PENDING tiếp theo
        // Dựa vào dateAdministered của mũi hiện tại và doseIntervalDays
        if (record.getDoseIntervalDays() != null && record.getDoseIntervalDays() > 0) {
            List<VaccinationDose> allDoses = vaccinationDoseRepository.findAllByVaccinationRecordIdOrderByDoseNumberAsc(record.getId());

            LocalDate newBaseDate = request.getDateAdministered();
            List<VaccinationDose> futureDosesToUpdate = new ArrayList<>();

            for (VaccinationDose dose : allDoses) {
                // Chỉ tịnh tiến các mũi có số thứ tự lớn hơn mũi vừa tiêm và đang ở trạng thái PENDING
                if (dose.getDoseNumber() > currentDose.getDoseNumber() && dose.getStatus() == DoseStatus.PENDING) {

                    // Khoảng cách từ mũi hiện tại (currentDose) đến mũi tương lai (dose)
                    // Công thức: mũi n cách mũi hiện tại k khoảng (k = doseNumber - currentDoseNumber)
                    int distanceMultiplier = dose.getDoseNumber() - currentDose.getDoseNumber();
                    long daysToAdd = (long) record.getDoseIntervalDays() * distanceMultiplier;

                    LocalDate newScheduledDate = newBaseDate.plusDays(daysToAdd);

                    log.info("Tịnh tiến Mũi {}: đổi lịch từ {} sang {}", dose.getDoseNumber(), dose.getScheduledDate(), newScheduledDate);

                    dose.setScheduledDate(newScheduledDate);
                    futureDosesToUpdate.add(dose);
                }
            }

            if (!futureDosesToUpdate.isEmpty()) {
                vaccinationDoseRepository.saveAll(futureDosesToUpdate);
            }
        }

        // TrÃ¡ÂºÂ£ vÃ¡Â»Â dÃ¡Â»Â¯ liÃ¡Â»â€¡u mÃ¡Â»â€ºi nhÃ¡ÂºÂ¥t
        List<VaccinationDose> updatedDoses = vaccinationDoseRepository.findAllByVaccinationRecordIdOrderByDoseNumberAsc(record.getId());

        evictDashboardCache(record.getHealthProfile());

        return vaccinationMapper.toRecordResponseWithDoses(record, updatedDoses);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VaccinationRecordResponse> getVaccinationHistory(Long profileId) {
        familySecurityUtil.checkUserBelongsToHealthProfile(profileId);

        List<VaccinationRecord> records = vaccinationRecordRepository.findAllByHealthProfileId(profileId);

        return records.stream().map(record -> {
            List<VaccinationDose> doses = vaccinationDoseRepository.findAllByVaccinationRecordIdOrderByDoseNumberAsc(record.getId());
            return vaccinationMapper.toRecordResponseWithDoses(record, doses);
        }).collect(Collectors.toList());
    }
}
