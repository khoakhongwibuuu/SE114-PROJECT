package com.carenest.backend.module.medication.service.impl;

import com.carenest.backend.common.exception.BadRequestException;
import com.carenest.backend.common.exception.ResourceNotFoundException;
import com.carenest.backend.module.cabinet.entity.CabinetMedicine;
import com.carenest.backend.module.cabinet.entity.MedicineCabinet;
import com.carenest.backend.module.cabinet.repository.CabinetMedicineRepository;
import com.carenest.backend.module.cabinet.repository.MedicineCabinetRepository;
import com.carenest.backend.module.family.util.FamilySecurityUtil;
import com.carenest.backend.module.healthprofile.entity.HealthProfile;
import com.carenest.backend.module.healthprofile.repository.HealthProfileRepository;
import com.carenest.backend.module.medication.dto.request.BatchCreateMedicationRequest;
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
import com.carenest.backend.module.ocr.dto.response.ParsedMedicationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.CacheManager;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicationServiceImpl implements MedicationService {

    private final MedicationRepository medicationRepository;
    private final MedicationLogRepository medicationLogRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final MedicineCabinetRepository medicineCabinetRepository;
    private final CabinetMedicineRepository cabinetMedicineRepository;
    private final MedicationMapper medicationMapper;
    private final FamilySecurityUtil familySecurityUtil;
    private final CacheManager cacheManager;

    private void evictDashboardCache(Long familyId) {
        if (familyId != null && cacheManager.getCache("dashboard") != null) {
            cacheManager.getCache("dashboard").evict(familyId);
        }
    }

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

        if (profile.getFamily() != null) {
            evictDashboardCache(profile.getFamily().getId());
        }

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

        if (medication.getHealthProfile().getFamily() != null) {
            evictDashboardCache(medication.getHealthProfile().getFamily().getId());
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

        if (medication.getHealthProfile().getFamily() != null) {
            evictDashboardCache(medication.getHealthProfile().getFamily().getId());
        }
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
            
            // Tự động trừ thuốc trong tủ của gia đình khi đánh dấu đã uống thuốc
            HealthProfile profile = log.getMedication().getHealthProfile();
            if (profile.getFamily() != null) {
                Long familyId = profile.getFamily().getId();
                medicineCabinetRepository.findByFamilyId(familyId).ifPresent(cabinet -> {
                    String medicineName = log.getMedication().getMedicineName();
                    cabinetMedicineRepository.findByCabinetIdAndMedicineNameIgnoreCase(cabinet.getId(), medicineName)
                            .ifPresent(cabMed -> {
                                int doseQty = parseDosageQuantity(log.getMedication().getDosage());
                                cabMed.setQuantity(Math.max(0, cabMed.getQuantity() - doseQty));
                                cabinetMedicineRepository.save(cabMed);
                            });
                });
            }
        }
        if (request.getNotes() != null) {
            log.setNotes(request.getNotes());
        }

        medicationLogRepository.save(log);

        if (log.getMedication().getHealthProfile().getFamily() != null) {
            evictDashboardCache(log.getMedication().getHealthProfile().getFamily().getId());
        }
    }

    private int parseDosageQuantity(String dosage) {
        if (dosage == null || dosage.trim().isEmpty()) {
            return 1; // Mặc định 1 nếu trống
        }
        
        String clean = dosage.trim().replaceAll(",", ".");
        
        // Khớp số hoặc phân số ở đầu chuỗi (ví dụ: "1", "2.5", "1/2", "0.5")
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^(\\d+(?:\\.\\d+)?|\\d+/\\d+)");
        java.util.regex.Matcher matcher = pattern.matcher(clean);
        
        if (matcher.find()) {
            String val = matcher.group(1);
            if (val.contains("/")) {
                String[] parts = val.split("/");
                try {
                    double num = Double.parseDouble(parts[0]);
                    double den = Double.parseDouble(parts[1]);
                    return (int) Math.max(1, Math.round(num / den));
                } catch (Exception e) {
                    return 1;
                }
            } else {
                try {
                    double valDouble = Double.parseDouble(val);
                    return (int) Math.max(1, Math.round(valDouble));
                } catch (Exception e) {
                    return 1;
                }
            }
        }
        return 1; // Fallback mặc định là 1 nếu không tìm thấy số
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
    @Override
    @Transactional
    public void createBatchFromOcr(BatchCreateMedicationRequest request) {
        // [QUY TẮC 3]: Xác nhận bảo mật sở hữu HealthProfile và Family
        familySecurityUtil.checkUserBelongsToFamily(request.getFamilyId());
        familySecurityUtil.checkUserBelongsToHealthProfile(request.getHealthProfileId());
        
        HealthProfile profile = healthProfileRepository.findById(request.getHealthProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Hồ sơ sức khỏe", request.getHealthProfileId()));

        // Tủ thuốc (Module 9)
        MedicineCabinet cabinet = medicineCabinetRepository.findByFamilyId(request.getFamilyId())
                .orElseGet(() -> {
                    MedicineCabinet newCabinet = MedicineCabinet.builder()
                            .family(profile.getFamily())
                            .name("Tủ thuốc gia đình")
                            .build();
                    return medicineCabinetRepository.save(newCabinet);
                });

        // Vòng lặp Transactional lưu hàng loạt
        for (ParsedMedicationDto dto : request.getMedications()) {
            // 1. Đồng bộ Tủ thuốc
            CabinetMedicine cabMed = cabinetMedicineRepository.findByCabinetIdAndMedicineNameIgnoreCase(cabinet.getId(), dto.getMedicineName())
                    .orElse(CabinetMedicine.builder()
                            .cabinet(cabinet)
                            .medicineName(dto.getMedicineName())
                            .unit(dto.getUnit() != null ? dto.getUnit() : "Viên")
                            .quantity(0)
                            .build());
            
            cabMed.setQuantity(cabMed.getQuantity() + (dto.getTotalQuantity() != null ? dto.getTotalQuantity() : 0));
            cabinetMedicineRepository.save(cabMed);

            // 2. Tạo Kế hoạch uống thuốc (Medication Plan)
            Medication medication = Medication.builder()
                    .healthProfile(profile)
                    .medicineName(dto.getMedicineName())
                    .dosage(dto.getDosage())
                    .frequency(dto.getFrequency() != null ? dto.getFrequency() : MedicationFrequency.DAILY)
                    .timesPerDay(dto.getTimesPerDay() != null ? dto.getTimesPerDay() : 1)
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(dto.getDurationDays() != null ? dto.getDurationDays() : 7))
                    .status(MedicationStatus.ACTIVE)
                    .notes(dto.getNotes())
                    // LLM might not know exact timeslots, we put a default one or leave empty and let User edit later
                    .timeSlots("08:00") // Default morning
                    .build();
            medication = medicationRepository.save(medication);

            // 3. Sinh Kế hoạch nhắc nhở (MedicationLog)
            generateLogsForMedication(medication);
        }

        evictDashboardCache(request.getFamilyId());
    }
}
