package com.carenest.backend.module.vaccination.repository;

import com.carenest.backend.module.vaccination.entity.VaccinationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VaccinationRecordRepository extends JpaRepository<VaccinationRecord, Long> {
    List<VaccinationRecord> findAllByHealthProfileId(Long profileId);
    Optional<VaccinationRecord> findByHealthProfileIdAndVaccineNameIgnoreCase(Long profileId, String vaccineName);
}
