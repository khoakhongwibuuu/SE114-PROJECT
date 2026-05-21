package com.carenest.backend.module.medication.repository;

import com.carenest.backend.module.medication.entity.Medication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {
    List<Medication> findByHealthProfileId(Long healthProfileId);
    List<Medication> findAllByHealthProfileId(Long healthProfileId);
    Page<Medication> findAllByHealthProfileId(Long healthProfileId, Pageable pageable);
}
