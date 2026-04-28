package com.carenest.backend.module.appointment.job;

import com.carenest.backend.module.appointment.entity.Appointment;
import com.carenest.backend.module.appointment.enums.AppointmentStatus;
import com.carenest.backend.module.appointment.repository.AppointmentRepository;
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
            
            // TODO: Call NotificationService to actually send a push notification/email
            // notificationService.sendAppointmentReminder(...)
            
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
