package com.carenest.backend.module.appointment.repository;

import com.carenest.backend.module.appointment.entity.Appointment;
import com.carenest.backend.module.appointment.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByHealthProfileIdOrderByAppointmentDateDesc(Long healthProfileId);

    List<Appointment> findByHealthProfileIdAndAppointmentDateAfterOrderByAppointmentDateAsc(Long healthProfileId, Instant date);

    List<Appointment> findByReminderSentFalseAndStatusAndAppointmentDateBetween(AppointmentStatus status, Instant start, Instant end);
}
