package com.carenest.backend.features.booking.repository;

import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.booking.entity.ConsultationThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository
public interface ConsultationThreadRepository extends JpaRepository<ConsultationThread, Long> {
    Optional<ConsultationThread> findByPatientAndDoctor(User patient, User doctor);
    java.util.List<ConsultationThread> findAllByPatientIdOrDoctorId(Long patientId, Long doctorId);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM ConsultationThread t WHERE t.id = :threadId AND (t.patient.id = :userId OR t.doctor.id = :userId)")
    boolean existsByIdAndParticipantId(@Param("threadId") Long threadId, @Param("userId") Long userId);
}
