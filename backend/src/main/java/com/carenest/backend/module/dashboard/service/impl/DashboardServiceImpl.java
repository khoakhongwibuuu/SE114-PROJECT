package com.carenest.backend.module.dashboard.service.impl;

import com.carenest.backend.module.dashboard.dto.response.DashboardResponse;
import com.carenest.backend.module.dashboard.dto.response.DashboardTask;
import com.carenest.backend.module.dashboard.service.DashboardService;
import com.carenest.backend.module.family.util.FamilySecurityUtil;
import com.carenest.backend.module.medication.entity.MedicationLog;
import com.carenest.backend.module.medication.enums.MedicationLogStatus;
import com.carenest.backend.module.medication.repository.MedicationLogRepository;
import com.carenest.backend.module.notification.repository.NotificationRepository;
import com.carenest.backend.module.vaccination.entity.VaccinationDose;
import com.carenest.backend.module.vaccination.enums.DoseStatus;
import com.carenest.backend.module.vaccination.repository.VaccinationDoseRepository;
import com.carenest.backend.module.appointment.entity.Appointment;
import com.carenest.backend.module.appointment.enums.AppointmentStatus;
import com.carenest.backend.module.appointment.repository.AppointmentRepository;
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
        // 1. Kiểm tra bảo mật: User hiện tại phải thuộc familyId này
        familySecurityUtil.checkUserBelongsToFamily(familyId);
        Long userId = familySecurityUtil.getCurrentUser().getId();

        // Nếu profileId != null, kiểm tra quyền truy cập profile đó
        if (profileId != null) {
            familySecurityUtil.checkHealthProfileBelongsToFamily(profileId, familyId);
        }

        // 2. Tính toán startOfDay và endOfDay (Instant)
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        LocalDate today = now.toLocalDate();
        Instant startOfDay = today.atStartOfDay(zoneId).toInstant();
        Instant endOfDay = today.plusDays(1).atStartOfDay(zoneId).toInstant();

        // 3. Tính toán khoảng ngày cho Tiêm chủng: [Hôm nay, Hôm nay + 2 ngày] (LocalDate)
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

        // 5. Query Vaccination Doses trong khoảng 2 ngày tới
        List<VaccinationDose> upcomingVaccines;
        if (profileId != null) {
            upcomingVaccines = vaccinationDoseRepository.findUpcomingDosesForProfileBetween(
                    profileId, DoseStatus.PENDING, today, todayPlusTwo);
        } else {
            upcomingVaccines = vaccinationDoseRepository.findUpcomingDosesForFamilyBetween(
                    familyId, DoseStatus.PENDING, today, todayPlusTwo);
        }

        // 6. Query Appointments trong ngày hôm nay
        List<Appointment> todayAppointments;
        if (profileId != null) {
            todayAppointments = appointmentRepository.findScheduledAppointmentsForProfileToday(
                    profileId, AppointmentStatus.SCHEDULED, startOfDay, endOfDay);
        } else {
            todayAppointments = appointmentRepository.findScheduledAppointmentsForFamilyToday(
                    familyId, AppointmentStatus.SCHEDULED, startOfDay, endOfDay);
        }

        // 7. Query count notifications chưa đọc
        long unreadCount = notificationRepository.countUnreadNotifications(userId);

        // 8. Map dữ liệu vào list chung DashboardTask
        List<DashboardTask> tasks = new ArrayList<>();

        // Map Thuốc
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

        // Map Tiêm chủng với tag nhắc nhở "⏳ Ngày mai" hoặc "⏳ Ngày kia"
        for (VaccinationDose dose : upcomingVaccines) {
            String subtitle = null;
            long daysBetween = ChronoUnit.DAYS.between(today, dose.getScheduledDate());
            if (daysBetween == 1) {
                subtitle = "⏳ Ngày mai";
            } else if (daysBetween == 2) {
                subtitle = "⏳ Ngày kia";
            } else if (daysBetween == 0) {
                subtitle = "⏳ Hôm nay";
            }

            tasks.add(DashboardTask.builder()
                    .type("VACCINATION")
                    .title(dose.getVaccinationRecord().getVaccineName() + " (Mũi " + dose.getDoseNumber() + ")")
                    .time(dose.getScheduledDate().atStartOfDay(zoneId).toInstant().toString())
                    .memberName(dose.getVaccinationRecord().getHealthProfile().getFullName())
                    .referenceId(dose.getId())
                    .profileId(dose.getVaccinationRecord().getHealthProfile().getId())
                    .subtitle(subtitle)
                    .build());
        }

        // Map Lịch khám
        for (Appointment app : todayAppointments) {
            String hospitalInfo = app.getHospitalName() != null ? " tại " + app.getHospitalName() : "";
            tasks.add(DashboardTask.builder()
                    .type("APPOINTMENT")
                    .title("Lịch khám bác sĩ " + (app.getDoctorName() != null ? app.getDoctorName() : "") + hospitalInfo)
                    .time(app.getAppointmentDate().toString())
                    .memberName(app.getHealthProfile().getFullName())
                    .referenceId(app.getId())
                    .profileId(app.getHealthProfile().getId())
                    .subtitle("🏥 Hôm nay")
                    .build());
        }

        // Sắp xếp các tasks theo thời gian
        tasks.sort(Comparator.comparing(DashboardTask::getTime));

        return DashboardResponse.builder()
                .unreadNotifications(unreadCount)
                .todayTasks(tasks)
                .build();
    }
}
