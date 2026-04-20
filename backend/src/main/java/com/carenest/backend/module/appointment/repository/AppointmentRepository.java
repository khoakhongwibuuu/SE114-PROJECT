package com.carenest.backend.module.appointment.repository;

import com.carenest.backend.module.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByHealthProfileId(Long healthProfileId);
}
