package com.carenest.backend.module.vaccination.repository;

import com.carenest.backend.module.vaccination.entity.VaccinationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VaccinationRecordRepository extends JpaRepository<VaccinationRecord, Long> {
    List<VaccinationRecord> findAllByHealthProfileId(Long profileId);
}
