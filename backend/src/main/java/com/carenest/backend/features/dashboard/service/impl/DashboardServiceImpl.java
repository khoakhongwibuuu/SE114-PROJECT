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
    public DashboardResponse getDashboardOverview(Long familyId, Long profileId) {
        Long userId = familySecurityUtil.getCurrentUser().getId();

        if (profileId != null) {
            familySecurityUtil.checkCanReadHealthProfile(profileId);
        } else if (familyId != null) {
            familySecurityUtil.checkUserBelongsToFamily(familyId);
        } else {
            familyId = familySecurityUtil.getDefaultFamilyId();
            if (familyId == null) {
                // If no family, try to get tasks for the user's personal profile at least
                com.carenest.backend.features.healthprofile.entity.HealthProfile personalProfile = 
                    com.carenest.backend.features.healthprofile.repository.HealthProfileRepository.class.cast(
                        org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext(
                            ((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()).getRequest().getServletContext()
                        ).getBean(com.carenest.backend.features.healthprofile.repository.HealthProfileRepository.class)
                    ).findFirstByUserIdAndFamilyIsNullAndIsChildFalseAndDeletedAtIsNull(userId).orElse(null);
                
                if (personalProfile != null) {
                    profileId = personalProfile.getId();
                } else {
                    return DashboardResponse.builder().unreadNotifications(0L).todayTasks(new ArrayList<>()).build();
                }
            } else {
                familySecurityUtil.checkUserBelongsToFamily(familyId);
            }
        }

        // 2. Tính toán startOfDay, endOfDay, endOfTomorrow (Instant)
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        LocalDate today = now.toLocalDate();
        Instant startOfDay = today.atStartOfDay(zoneId).toInstant();
        Instant endOfDay = today.plusDays(1).atStartOfDay(zoneId).toInstant();
        Instant endOfTomorrow = today.plusDays(2).atStartOfDay(zoneId).toInstant();

        // 3. Tính toán khoảng ngày cho Tiêm chủng: [Hôm nay, Ngày mai] (LocalDate)
        LocalDate tomorrowDate = today.plusDays(1);

        // 4. Query Medication Logs
        List<MedicationLog> pendingMedications;
        if (profileId != null) {
            pendingMedications = medicationLogRepository.findPendingTasksForProfileToday(
                    profileId, MedicationLogStatus.PENDING, startOfDay, endOfTomorrow);
        } else {
            pendingMedications = medicationLogRepository.findPendingTasksForFamilyToday(
                    familyId, MedicationLogStatus.PENDING, startOfDay, endOfTomorrow);
        }

        // 5. Query Vaccination Doses trong hôm nay và ngày mai
        List<VaccinationDose> upcomingVaccines;
        if (profileId != null) {
            upcomingVaccines = vaccinationDoseRepository.findUpcomingDosesForProfileBetween(
                    profileId, DoseStatus.PENDING, today, tomorrowDate);
        } else {
            upcomingVaccines = vaccinationDoseRepository.findUpcomingDosesForFamilyBetween(
                    familyId, DoseStatus.PENDING, today, tomorrowDate);
        }

        // 6. Query Appointments trong ngày hôm nay và ngày mai
        List<Appointment> todayAppointments;
        if (profileId != null) {
            todayAppointments = appointmentRepository.findScheduledAppointmentsForProfileToday(
                    profileId, AppointmentStatus.SCHEDULED, startOfDay, endOfTomorrow);
        } else {
            todayAppointments = appointmentRepository.findScheduledAppointmentsForFamilyToday(
                    familyId, AppointmentStatus.SCHEDULED, startOfDay, endOfTomorrow);
        }

        // 7. Query count notifications chưa đọc
        long unreadCount = notificationRepository.countUnreadNotifications(userId);

        // 8. Map dữ liệu vào list chung
        List<DashboardTask> todayTasksList = new ArrayList<>();
        List<DashboardTask> tomorrowTasksList = new ArrayList<>();

        // Map Thuốc
        for (MedicationLog logItem : pendingMedications) {
            DashboardTask task = DashboardTask.builder()
                    .type("MEDICATION")
                    .title(logItem.getMedication().getMedicineName())
                    .time(logItem.getScheduledTime().toString())
                    .memberName(logItem.getMedication().getHealthProfile().getFullName())
                    .referenceId(logItem.getId())
                    .profileId(logItem.getMedication().getHealthProfile().getId())
                    .build();
            if (logItem.getScheduledTime().isBefore(endOfDay)) {
                todayTasksList.add(task);
            } else {
                tomorrowTasksList.add(task);
            }
        }

        // Map Tiêm chủng với tag nhắc nhở "⏳ Ngày mai" hoặc "⏳ Hôm nay"
        for (VaccinationDose dose : upcomingVaccines) {
            long daysBetween = ChronoUnit.DAYS.between(today, dose.getScheduledDate());
            String subtitle = (daysBetween == 1) ? "⏳ Ngày mai" : "⏳ Hôm nay";

            DashboardTask task = DashboardTask.builder()
                    .type("VACCINATION")
                    .title(dose.getVaccinationRecord().getVaccineName() + " (Mũi " + dose.getDoseNumber() + ")")
                    .time(dose.getScheduledDate().atStartOfDay(zoneId).toInstant().toString())
                    .memberName(dose.getVaccinationRecord().getHealthProfile().getFullName())
                    .referenceId(dose.getId())
                    .profileId(dose.getVaccinationRecord().getHealthProfile().getId())
                    .subtitle(subtitle)
                    .build();
            if (daysBetween == 0) {
                todayTasksList.add(task);
            } else {
                tomorrowTasksList.add(task);
            }
        }

        // Map Lịch khám
        for (Appointment app : todayAppointments) {
            String hospitalInfo = app.getHospitalName() != null ? " tại " + app.getHospitalName() : "";
            boolean isToday = app.getAppointmentDate().isBefore(endOfDay);
            String subtitle = isToday ? "🏥 Hôm nay" : "🏥 Ngày mai";

            DashboardTask task = DashboardTask.builder()
                    .type("APPOINTMENT")
                    .title("Lịch khám bác sĩ " + (app.getDoctorName() != null ? app.getDoctorName() : "") + hospitalInfo)
                    .time(app.getAppointmentDate().toString())
                    .memberName(app.getHealthProfile().getFullName())
                    .referenceId(app.getId())
                    .profileId(app.getHealthProfile().getId())
                    .subtitle(subtitle)
                    .build();
            if (isToday) {
                todayTasksList.add(task);
            } else {
                tomorrowTasksList.add(task);
            }
        }

        // Sắp xếp các tasks theo thời gian
        todayTasksList.sort(Comparator.comparing(DashboardTask::getTime));
        tomorrowTasksList.sort(Comparator.comparing(DashboardTask::getTime));

        return DashboardResponse.builder()
                .unreadNotifications(unreadCount)
                .todayTasks(todayTasksList)
                .tomorrowTasks(tomorrowTasksList)
                .build();
    }
}
