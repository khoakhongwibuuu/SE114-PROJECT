package com.carenest.backend.features.dashboard.service.impl;

import com.carenest.backend.features.dashboard.dto.response.DashboardResponse;
import com.carenest.backend.features.dashboard.dto.response.DashboardTask;
import com.carenest.backend.features.dashboard.service.DashboardService;
import com.carenest.backend.features.family.util.FamilySecurityUtil;
import com.carenest.backend.features.medication.entity.MedicationLog;
import com.carenest.backend.features.medication.enums.MedicationLogStatus;
import com.carenest.backend.features.medication.repository.MedicationLogRepository;
import com.carenest.backend.features.notification.repository.NotificationRepository;
import com.carenest.backend.features.vaccination.entity.VaccinationDose;
import com.carenest.backend.features.vaccination.enums.DoseStatus;
import com.carenest.backend.features.vaccination.repository.VaccinationDoseRepository;
import com.carenest.backend.features.appointment.entity.Appointment;
import com.carenest.backend.features.appointment.enums.AppointmentStatus;
import com.carenest.backend.features.appointment.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final MedicationLogRepository medicationLogRepository;
    private final VaccinationDoseRepository vaccinationDoseRepository;
    private final NotificationRepository notificationRepository;
    private final AppointmentRepository appointmentRepository;
    private final FamilySecurityUtil familySecurityUtil;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dashboard", key = "#familyId", condition = "#profileId == null && #familyId != null")
    public DashboardResponse getDashboardOverview(Long familyId, Long profileId) {
        // 1. Kiá»ƒm tra báº£o máº­t: User hiá»‡n táº¡i pháº£i thuá»™c familyId nÃ y
        familySecurityUtil.checkUserBelongsToFamily(familyId);
        Long userId = familySecurityUtil.getCurrentUser().getId();

        // Náº¿u profileId != null, kiá»ƒm tra quyá»n truy cáº­p profile Ä‘Ã³
        if (profileId != null) {
            familySecurityUtil.checkHealthProfileBelongsToFamily(profileId, familyId);
        }

        // 2. TÃ­nh toÃ¡n startOfDay vÃ  endOfDay (Instant)
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        LocalDate today = now.toLocalDate();
        Instant startOfDay = today.atStartOfDay(zoneId).toInstant();
        Instant endOfDay = today.plusDays(1).atStartOfDay(zoneId).toInstant();

        // 3. TÃ­nh toÃ¡n khoáº£ng ngÃ y cho TiÃªm chá»§ng: [HÃ´m nay, HÃ´m nay + 2 ngÃ y] (LocalDate)
        LocalDate todayPlusTwo = today.plusDays(2);

        // 4. Query Medication Logs
        List<MedicationLog> pendingMedications;
        if (profileId != null) {
            pendingMedications = medicationLogRepository.findPendingTasksForProfileToday(
                    profileId, MedicationLogStatus.PENDING, startOfDay, endOfDay);
        } else {
            pendingMedications = medicationLogRepository.findPendingTasksForFamilyToday(
                    familyId, MedicationLogStatus.PENDING, startOfDay, endOfDay);
        }

        // 5. Query Vaccination Doses trong khoáº£ng 2 ngÃ y tá»›i
        List<VaccinationDose> upcomingVaccines;
        if (profileId != null) {
            upcomingVaccines = vaccinationDoseRepository.findUpcomingDosesForProfileBetween(
                    profileId, DoseStatus.PENDING, today, todayPlusTwo);
        } else {
            upcomingVaccines = vaccinationDoseRepository.findUpcomingDosesForFamilyBetween(
                    familyId, DoseStatus.PENDING, today, todayPlusTwo);
        }

        // 6. Query Appointments trong ngÃ y hÃ´m nay
        List<Appointment> todayAppointments;
        if (profileId != null) {
            todayAppointments = appointmentRepository.findScheduledAppointmentsForProfileToday(
                    profileId, AppointmentStatus.SCHEDULED, startOfDay, endOfDay);
        } else {
            todayAppointments = appointmentRepository.findScheduledAppointmentsForFamilyToday(
                    familyId, AppointmentStatus.SCHEDULED, startOfDay, endOfDay);
        }

        // 7. Query count notifications chÆ°a Ä‘á»c
        long unreadCount = notificationRepository.countUnreadNotifications(userId);

        // 8. Map dá»¯ liá»‡u vÃ o list chung DashboardTask
        List<DashboardTask> tasks = new ArrayList<>();

        // Map Thuá»‘c
        for (MedicationLog logItem : pendingMedications) {
            tasks.add(DashboardTask.builder()
                    .type("MEDICATION")
                    .title(logItem.getMedication().getMedicineName())
                    .time(logItem.getScheduledTime().toString())
                    .memberName(logItem.getMedication().getHealthProfile().getFullName())
                    .referenceId(logItem.getId())
                    .profileId(logItem.getMedication().getHealthProfile().getId())
                    .build());
        }

        // Map TiÃªm chá»§ng vá»›i tag nháº¯c nhá»Ÿ "â³ NgÃ y mai" hoáº·c "â³ NgÃ y kia"
        for (VaccinationDose dose : upcomingVaccines) {
            String subtitle = null;
            long daysBetween = ChronoUnit.DAYS.between(today, dose.getScheduledDate());
            if (daysBetween == 1) {
                subtitle = "â³ NgÃ y mai";
            } else if (daysBetween == 2) {
                subtitle = "â³ NgÃ y kia";
            } else if (daysBetween == 0) {
                subtitle = "â³ HÃ´m nay";
            }

            tasks.add(DashboardTask.builder()
                    .type("VACCINATION")
                    .title(dose.getVaccinationRecord().getVaccineName() + " (MÅ©i " + dose.getDoseNumber() + ")")
                    .time(dose.getScheduledDate().atStartOfDay(zoneId).toInstant().toString())
                    .memberName(dose.getVaccinationRecord().getHealthProfile().getFullName())
                    .referenceId(dose.getId())
                    .profileId(dose.getVaccinationRecord().getHealthProfile().getId())
                    .subtitle(subtitle)
                    .build());
        }

        // Map Lá»‹ch khÃ¡m
        for (Appointment app : todayAppointments) {
            String hospitalInfo = app.getHospitalName() != null ? " táº¡i " + app.getHospitalName() : "";
            tasks.add(DashboardTask.builder()
                    .type("APPOINTMENT")
                    .title("Lá»‹ch khÃ¡m bÃ¡c sÄ© " + (app.getDoctorName() != null ? app.getDoctorName() : "") + hospitalInfo)
                    .time(app.getAppointmentDate().toString())
                    .memberName(app.getHealthProfile().getFullName())
                    .referenceId(app.getId())
                    .profileId(app.getHealthProfile().getId())
                    .subtitle("ðŸ¥ HÃ´m nay")
                    .build());
        }

        // Sáº¯p xáº¿p cÃ¡c tasks theo thá»i gian
        tasks.sort(Comparator.comparing(DashboardTask::getTime));

        return DashboardResponse.builder()
                .unreadNotifications(unreadCount)
                .todayTasks(tasks)
                .build();
    }
}
