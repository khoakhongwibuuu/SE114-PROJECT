package com.carenest.backend.module.appointment.job;

import com.carenest.backend.module.appointment.entity.Appointment;
import com.carenest.backend.module.appointment.enums.AppointmentStatus;
import com.carenest.backend.module.appointment.repository.AppointmentRepository;
import com.carenest.backend.module.notification.enums.NotificationType;
import com.carenest.backend.module.notification.service.NotificationService;
import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.family.entity.Family;
import com.carenest.backend.module.family.entity.FamilyMember;
import com.carenest.backend.module.family.repository.FamilyMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentReminderJob {

    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;
    private final FamilyMemberRepository familyMemberRepository;

    /**
     * Cronjob runs every hour at minute 0.
     * Scans for SCHEDULED appointments that are within the next 24 hours
     * and haven't had a reminder sent yet.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void sendAppointmentReminders() {
        log.info("Starting Appointment Reminder Job...");

        Instant now = Instant.now();
        Instant in24Hours = now.plus(24, ChronoUnit.HOURS);

        List<Appointment> upcomingAppointments = appointmentRepository
                .findByReminderSentFalseAndStatusAndAppointmentDateBetween(
                        AppointmentStatus.SCHEDULED, now, in24Hours);

        for (Appointment appointment : upcomingAppointments) {
            log.info("Sending reminder for appointment ID: {} for profile: {}", 
                    appointment.getId(), appointment.getHealthProfile().getFullName());
            String title = "Nhắc nhở lịch khám";
            String message = String.format("Bạn có lịch khám cho %s với bác sĩ %s tại %s.",
                    appointment.getHealthProfile().getFullName(),
                    appointment.getDoctorName() != null ? appointment.getDoctorName() : "chưa rõ",
                    appointment.getHospitalName() != null ? appointment.getHospitalName() : "chưa rõ"
            );
            
            Family family = appointment.getHealthProfile().getFamily();
            if (family != null) {
                List<User> targetUsers = familyMemberRepository.findAllByFamilyId(family.getId())
                        .stream().map(FamilyMember::getUser).toList();
                notificationService.createNotificationForUsers(targetUsers, title, message, NotificationType.APPOINTMENT, "APPOINTMENT", appointment.getId());
            } else if (appointment.getHealthProfile().getUser() != null) {
                notificationService.createNotificationForUser(appointment.getHealthProfile().getUser(), title, message, NotificationType.APPOINTMENT, "APPOINTMENT", appointment.getId());
            }
            appointment.setReminderSent(true);
        }

        if (!upcomingAppointments.isEmpty()) {
            appointmentRepository.saveAll(upcomingAppointments);
            log.info("Successfully sent reminders for {} appointments.", upcomingAppointments.size());
        } else {
            log.info("No upcoming appointments require reminders at this time.");
        }
    }
}
