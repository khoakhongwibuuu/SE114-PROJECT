package com.carenest.backend.module.medication.repository;

import com.carenest.backend.module.medication.entity.MedicationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MedicationLogRepository extends JpaRepository<MedicationLog, Long> {
    List<MedicationLog> findAllByMedicationId(Long medicationId);
    List<MedicationLog> findAllByMedicationIdAndScheduledTimeBetween(Long medicationId, Instant start, Instant end);
    boolean existsByMedicationIdAndScheduledTime(Long medicationId, Instant scheduledTime);
    List<MedicationLog> findAllByStatusAndIsNotifiedFalseAndScheduledTimeBetween(com.carenest.backend.module.medication.enums.MedicationLogStatus status, Instant start, Instant end);

    @Query("SELECT m FROM MedicationLog m " +
           "JOIN FETCH m.medication med " +
           "JOIN FETCH med.healthProfile hp " +
           "WHERE hp.family.id = :familyId " +
           "AND m.status = :status " +
           "AND m.scheduledTime BETWEEN :startOfDay AND :endOfDay " +
           "ORDER BY m.scheduledTime ASC")
    List<MedicationLog> findPendingTasksForFamilyToday(
            @Param("familyId") Long familyId,
            @Param("status") com.carenest.backend.module.medication.enums.MedicationLogStatus status,
            @Param("startOfDay") Instant startOfDay,
            @Param("endOfDay") Instant endOfDay);
}
