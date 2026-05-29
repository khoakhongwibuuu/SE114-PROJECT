package com.carenest.backend.features.appointment.repository;

import com.carenest.backend.features.appointment.entity.Appointment;
import com.carenest.backend.features.appointment.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByHealthProfileIdOrderByAppointmentDateDesc(Long healthProfileId);

    List<Appointment> findByHealthProfileIdAndAppointmentDateAfterOrderByAppointmentDateAsc(Long healthProfileId, Instant date);

    List<Appointment> findByReminderSentFalseAndStatusAndAppointmentDateBetween(AppointmentStatus status, Instant start, Instant end);

    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.healthProfile hp " +
           "WHERE (hp.family.id = :familyId OR hp.user.id IN (SELECT fm.user.id FROM FamilyMember fm WHERE fm.family.id = :familyId)) " +
           "AND a.status = :status " +
           "AND a.appointmentDate BETWEEN :startOfDay AND :endOfDay " +
           "ORDER BY a.appointmentDate ASC")
    List<Appointment> findScheduledAppointmentsForFamilyToday(
            @Param("familyId") Long familyId,
            @Param("status") AppointmentStatus status,
            @Param("startOfDay") Instant startOfDay,
            @Param("endOfDay") Instant endOfDay);

    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.healthProfile hp " +
           "WHERE hp.id = :profileId " +
           "AND a.status = :status " +
           "AND a.appointmentDate BETWEEN :startOfDay AND :endOfDay " +
           "ORDER BY a.appointmentDate ASC")
    List<Appointment> findScheduledAppointmentsForProfileToday(
            @Param("profileId") Long profileId,
            @Param("status") AppointmentStatus status,
            @Param("startOfDay") Instant startOfDay,
            @Param("endOfDay") Instant endOfDay);
}
