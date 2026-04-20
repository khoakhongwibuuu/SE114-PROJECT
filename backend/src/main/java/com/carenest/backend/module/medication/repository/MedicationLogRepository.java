package com.carenest.backend.module.medication.repository;

import com.carenest.backend.module.medication.entity.MedicationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicationLogRepository extends JpaRepository<MedicationLog, Long> {
}
