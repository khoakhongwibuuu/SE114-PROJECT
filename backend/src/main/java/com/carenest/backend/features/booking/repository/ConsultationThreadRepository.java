package com.carenest.backend.features.booking.repository;

import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.booking.entity.ConsultationThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsultationThreadRepository extends JpaRepository<ConsultationThread, Long> {
    Optional<ConsultationThread> findByPatientAndDoctor(User patient, User doctor);
    java.util.List<ConsultationThread> findAllByPatientIdOrDoctorId(Long patientId, Long doctorId);
}
