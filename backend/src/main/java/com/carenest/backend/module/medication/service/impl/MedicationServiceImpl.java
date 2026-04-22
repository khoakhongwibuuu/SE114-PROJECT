package com.carenest.backend.module.medication.service.impl;

import com.carenest.backend.common.exception.BadRequestException;
import com.carenest.backend.common.exception.ResourceNotFoundException;
import com.carenest.backend.module.family.util.FamilySecurityUtil;
import com.carenest.backend.module.healthprofile.entity.HealthProfile;
import com.carenest.backend.module.healthprofile.repository.HealthProfileRepository;
import com.carenest.backend.module.medication.dto.request.CheckInMedicationRequest;
import com.carenest.backend.module.medication.dto.request.CreateMedicationRequest;
import com.carenest.backend.module.medication.dto.request.UpdateMedicationRequest;
import com.carenest.backend.module.medication.dto.response.MedicationLogResponse;
import com.carenest.backend.module.medication.dto.response.MedicationResponse;
import com.carenest.backend.module.medication.entity.Medication;
import com.carenest.backend.module.medication.entity.MedicationLog;
import com.carenest.backend.module.medication.enums.MedicationFrequency;
import com.carenest.backend.module.medication.enums.MedicationLogStatus;
import com.carenest.backend.module.medication.enums.MedicationStatus;
import com.carenest.backend.module.medication.mapper.MedicationMapper;
import com.carenest.backend.module.medication.repository.MedicationLogRepository;
import com.carenest.backend.module.medication.repository.MedicationRepository;
import com.carenest.backend.module.medication.service.MedicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicationServiceImpl implements MedicationService {

    private final MedicationRepository medicationRepository;
    private final MedicationLogRepository medicationLogRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final MedicationMapper medicationMapper;
    private final FamilySecurityUtil familySecurityUtil;

    @Override
    @Transactional
    public MedicationResponse createMedication(Long profileId, CreateMedicationRequest request) {
        familySecurityUtil.checkUserBelongsToHealthProfile(profileId);

        HealthProfile profile = healthProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("HealthProfile", profileId));

        validateDates(request.getStartDate(), request.getEndDate());
        validateTimeSlots(request.getTimeSlots());

        Medication medication = medicationMapper.toEntity(request);
        medication.setHealthProfile(profile);
        medication.setStatus(MedicationStatus.ACTIVE);

        medication = medicationRepository.save(medication);

        // Generate Logs
        generateLogsForMedication(medication);

        return medicationMapper.toMedicationResponse(medication);
    }

    @Override
    @Transactional
    public MedicationResponse updateMedication(Long medicationId, UpdateMedicationRequest request) {
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Medication", medicationId));

        familySecurityUtil.checkUserBelongsToHealthProfile(medication.getHealthProfile().getId());

        boolean scheduleChanged = false;

        if (request.getMedicineName() != null) medication.setMedicineName(request.getMedicineName());
        if (request.getDosage() != null) medication.setDosage(request.getDosage());
        if (request.getNotes() != null) medication.setNotes(request.getNotes());

        if (request.getFrequency() != null && request.getFrequency() != medication.getFrequency()) {
            medication.setFrequency(request.getFrequency());
            scheduleChanged = true;
        }
        if (request.getTimesPerDay() != null) {
            medication.setTimesPerDay(request.getTimesPerDay());
            scheduleChanged = true;
        }
        if (request.getTimeSlots() != null) {
            validateTimeSlots(request.getTimeSlots());
            medication.setTimeSlots(medicationMapper.listToString(request.getTimeSlots()));
            scheduleChanged = true;
        }
        if (request.getStartDate() != null || request.getEndDate() != null) {
            LocalDate start = request.getStartDate() != null ? request.getStartDate() : medication.getStartDate();
            LocalDate end = request.getEndDate() != null ? request.getEndDate() : medication.getEndDate();
            validateDates(start, end);
            medication.setStartDate(start);
            medication.setEndDate(end);
            scheduleChanged = true;
        }

        medication = medicationRepository.save(medication);

        if (scheduleChanged) {
            // Cascade Delete future pending logs and regenerate
            Instant now = Instant.now();
            List<MedicationLog> futureLogs = medicationLogRepository.findAllByMedicationId(medicationId)
                    .stream()
                    .filter(log -> log.getStatus() == MedicationLogStatus.PENDING && log.getScheduledTime().isAfter(now))
                    .collect(Collectors.toList());
            medicationLogRepository.deleteAll(futureLogs);

            // Re-generate from today onwards
            generateLogsForMedication(medication);
        }

        return medicationMapper.toMedicationResponse(medication);
    }

    @Override
    @Transactional
    public void completeMedication(Long medicationId) {
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Medication", medicationId));

        familySecurityUtil.checkUserBelongsToHealthProfile(medication.getHealthProfile().getId());

        medication.setStatus(MedicationStatus.COMPLETED);
        medicationRepository.save(medication);

        // Cascade delete future pending logs
        Instant now = Instant.now();
        List<MedicationLog> futureLogs = medicationLogRepository.findAllByMedicationId(medicationId)
                .stream()
                .filter(log -> log.getStatus() == MedicationLogStatus.PENDING && log.getScheduledTime().isAfter(now))
                .collect(Collectors.toList());
        medicationLogRepository.deleteAll(futureLogs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationResponse> getMedicationsByProfile(Long profileId) {
        familySecurityUtil.checkUserBelongsToHealthProfile(profileId);

        return medicationRepository.findAllByHealthProfileId(profileId)
                .stream()
                .map(medicationMapper::toMedicationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationLogResponse> getMedicationsForToday(Long profileId) {
        familySecurityUtil.checkUserBelongsToHealthProfile(profileId);

        // Today's range in local timezone (assuming system default or Asia/Ho_Chi_Minh for simplicity)
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        Instant startOfDay = today.atStartOfDay(zone).toInstant();
        Instant endOfDay = today.plusDays(1).atStartOfDay(zone).toInstant();

        // Get active medications for profile
        List<Medication> activeMeds = medicationRepository.findAllByHealthProfileId(profileId)
                .stream()
                .filter(m -> m.getStatus() == MedicationStatus.ACTIVE)
                .collect(Collectors.toList());

        List<MedicationLog> todaysLogs = new ArrayList<>();
        for (Medication med : activeMeds) {
             todaysLogs.addAll(medicationLogRepository.findAllByMedicationIdAndScheduledTimeBetween(med.getId(), startOfDay, endOfDay));
        }

        return todaysLogs.stream()
                .map(medicationMapper::toMedicationLogResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void checkInMedicationLog(Long logId, CheckInMedicationRequest request) {
        MedicationLog log = medicationLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("MedicationLog", logId));

        familySecurityUtil.checkUserBelongsToHealthProfile(log.getMedication().getHealthProfile().getId());

        if (log.getStatus() != MedicationLogStatus.PENDING) {
             throw new BadRequestException("Bản ghi này đã được xử lý (đã uống hoặc bỏ qua)");
        }

        log.setStatus(request.getStatus());
        if (request.getStatus() == MedicationLogStatus.TAKEN) {
            log.setTakenTime(Instant.now());
        }
        if (request.getNotes() != null) {
            log.setNotes(request.getNotes());
        }

        medicationLogRepository.save(log);
    }

    private void generateLogsForMedication(Medication medication) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate currentGenDate = LocalDate.now(zone);
        
        // Start from today or startDate, whichever is later
        if (medication.getStartDate().isAfter(currentGenDate)) {
            currentGenDate = medication.getStartDate();
        }

        LocalDate calcEndDate = medication.getEndDate();
        LocalDate maxEndDate = currentGenDate.plusDays(30);

        if (calcEndDate == null || calcEndDate.isAfter(maxEndDate)) {
            calcEndDate = maxEndDate;
        }

        List<String> timeSlots = medicationMapper.stringToList(medication.getTimeSlots());
        if (timeSlots == null || timeSlots.isEmpty()) return;

        List<MedicationLog> newLogs = new ArrayList<>();
        int stepDays = getStepDays(medication.getFrequency());

        // Simple scheduling: jump by stepDays
        // For EXACT_DAYS (Specific days of week), logic would be more complex. Here we handle basic intervals.
        while (!currentGenDate.isAfter(calcEndDate)) {
             boolean shouldGenerateToday = true;

             if (medication.getFrequency() == MedicationFrequency.SPECIFIC_DAYS) {
                 // Simplified: If frequency is SPECIFIC_DAYS but we don't have day-of-week data, we skip.
                 // In a full system, you'd store Monday,Wednesday,Friday.
             }

             if (shouldGenerateToday) {
                 for (String timeStr : timeSlots) {
                     LocalTime time = LocalTime.parse(timeStr); // format HH:mm
                     Instant scheduledTime = currentGenDate.atTime(time).atZone(zone).toInstant();
                     
                     // Don't generate if time already passed today
                     if (scheduledTime.isAfter(Instant.now())) {
                         // Check if log already exists to prevent duplicates on update
                         if (!medicationLogRepository.existsByMedicationIdAndScheduledTime(medication.getId(), scheduledTime)) {
                             MedicationLog log = MedicationLog.builder()
                                     .medication(medication)
                                     .scheduledTime(scheduledTime)
                                     .status(MedicationLogStatus.PENDING)
                                     .build();
                             newLogs.add(log);
                         }
                     }
                 }
             }
             
             if (stepDays > 0) {
                 currentGenDate = currentGenDate.plusDays(stepDays);
             } else {
                 break; // Prevent infinite loop if frequency logic fails
             }
        }

        if (!newLogs.isEmpty()) {
            medicationLogRepository.saveAll(newLogs);
        }
    }

    private int getStepDays(MedicationFrequency frequency) {
        switch (frequency) {
            case AS_NEEDED: return 0; // No schedule
            case EVERY_OTHER_DAY: return 2;
            case DAILY:
            case SPECIFIC_DAYS: 
            default:
                return 1;
        }
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (end != null && end.isBefore(start)) {
            throw new BadRequestException("Ngày kết thúc không được trước ngày bắt đầu");
        }
    }

    private void validateTimeSlots(List<String> timeSlots) {
        if (timeSlots == null || timeSlots.isEmpty()) return;
        for (String time : timeSlots) {
            try {
                LocalTime.parse(time);
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Định dạng giờ không hợp lệ. Vui lòng dùng HH:mm (VD: 08:00)");
            }
        }
    }
}
