package com.carenest.backend.module.vaccination.repository;

import com.carenest.backend.module.vaccination.entity.VaccinationDose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VaccinationDoseRepository extends JpaRepository<VaccinationDose, Long> {
    List<VaccinationDose> findAllByVaccinationRecordIdOrderByDoseNumberAsc(Long vaccinationRecordId);
}
