package com.carenest.backend.module.vaccination.service.impl;

import com.carenest.backend.common.exception.BadRequestException;
import com.carenest.backend.common.exception.ResourceNotFoundException;
import com.carenest.backend.module.family.util.FamilySecurityUtil;
import com.carenest.backend.module.healthprofile.entity.HealthProfile;
import com.carenest.backend.module.healthprofile.repository.HealthProfileRepository;
import com.carenest.backend.module.vaccination.dto.request.AdministerDoseRequest;
import com.carenest.backend.module.vaccination.dto.request.CreateVaccinationRequest;
import com.carenest.backend.module.vaccination.dto.response.VaccinationRecordResponse;
import com.carenest.backend.module.vaccination.entity.VaccinationDose;
import com.carenest.backend.module.vaccination.entity.VaccinationRecord;
import com.carenest.backend.module.vaccination.enums.DoseStatus;
import com.carenest.backend.module.vaccination.mapper.VaccinationMapper;
import com.carenest.backend.module.vaccination.repository.VaccinationDoseRepository;
import com.carenest.backend.module.vaccination.repository.VaccinationRecordRepository;
import com.carenest.backend.module.vaccination.service.VaccinationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public VaccinationRecordResponse createVaccinationPlan(Long profileId, CreateVaccinationRequest request) {
        familySecurityUtil.checkUserBelongsToHealthProfile(profileId);

        HealthProfile profile = healthProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("HealthProfile", profileId));

        if (request.getTotalDoses() > 1 && (request.getDoseIntervalDays() == null || request.getDoseIntervalDays() <= 0)) {
            throw new BadRequestException("Khoảng cách giữa các mũi tiêm (doseIntervalDays) phải lớn hơn 0 nếu có nhiều hơn 1 mũi.");
        }

        VaccinationRecord record = vaccinationMapper.toEntity(request);
        record.setHealthProfile(profile);
        record = vaccinationRecordRepository.save(record);

        // Pre-generation logic: Sinh sẵn toàn bộ các mũi tiêm (PENDING)
        List<VaccinationDose> doses = new ArrayList<>();
        LocalDate currentDate = request.getStartDate();

        for (int i = 1; i <= request.getTotalDoses(); i++) {
            VaccinationDose dose = VaccinationDose.builder()
                    .vaccinationRecord(record)
                    .doseNumber(i)
                    .scheduledDate(currentDate)
                    .status(DoseStatus.PENDING)
                    .location(request.getLocation())
                    .build();
            doses.add(dose);

            if (request.getDoseIntervalDays() != null) {
                currentDate = currentDate.plusDays(request.getDoseIntervalDays());
            }
        }

        doses = vaccinationDoseRepository.saveAll(doses);
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

        // Logic Tịnh tiến ngày tiêm (Auto-Rescheduling)
        // Yêu cầu từ Tech Lead: Cập nhật lại scheduledDate cho các mũi PENDING tiếp theo
        // Dựa vào dateAdministered của mũi hiện tại và doseIntervalDays
        if (record.getDoseIntervalDays() != null && record.getDoseIntervalDays() > 0) {
            List<VaccinationDose> allDoses = vaccinationDoseRepository.findAllByVaccinationRecordIdOrderByDoseNumberAsc(record.getId());
            
            LocalDate newBaseDate = request.getDateAdministered();
            List<VaccinationDose> futureDosesToUpdate = new ArrayList<>();

            for (VaccinationDose dose : allDoses) {
                // Chỉ tịnh tiến các mũi có số thứ tự lớn hơn mũi vừa tiêm VÀ đang ở trạng thái PENDING
                if (dose.getDoseNumber() > currentDose.getDoseNumber() && dose.getStatus() == DoseStatus.PENDING) {
                    
                    // Khoảng cách từ mũi hiện tại (currentDose) đến mũi tương lai (dose)
                    // Công thức: Mũi n cách mũi hiện tại k khoảng (k = doseNumber - currentDoseNumber)
                    int distanceMultiplier = dose.getDoseNumber() - currentDose.getDoseNumber();
                    long daysToAdd = (long) record.getDoseIntervalDays() * distanceMultiplier;
                    
                    LocalDate newScheduledDate = newBaseDate.plusDays(daysToAdd);
                    
                    log.info("Tịnh tiến Mũi {}: Đổi lịch từ {} sang {}", dose.getDoseNumber(), dose.getScheduledDate(), newScheduledDate);
                    
                    dose.setScheduledDate(newScheduledDate);
                    futureDosesToUpdate.add(dose);
                }
            }

            if (!futureDosesToUpdate.isEmpty()) {
                vaccinationDoseRepository.saveAll(futureDosesToUpdate);
            }
        }

        // Trả về dữ liệu mới nhất
        List<VaccinationDose> updatedDoses = vaccinationDoseRepository.findAllByVaccinationRecordIdOrderByDoseNumberAsc(record.getId());
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
