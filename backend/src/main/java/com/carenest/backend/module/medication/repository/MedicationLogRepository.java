package com.carenest.backend.module.medication.repository;

import com.carenest.backend.module.medication.entity.MedicationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MedicationLogRepository extends JpaRepository<MedicationLog, Long> {
    List<MedicationLog> findAllByMedicationId(Long medicationId);
    List<MedicationLog> findAllByMedicationIdAndScheduledTimeBetween(Long medicationId, Instant start, Instant end);
    boolean existsByMedicationIdAndScheduledTime(Long medicationId, Instant scheduledTime);
}
