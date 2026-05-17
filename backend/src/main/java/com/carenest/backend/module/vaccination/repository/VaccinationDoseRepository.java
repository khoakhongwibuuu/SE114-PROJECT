package com.carenest.backend.module.vaccination.repository;

import com.carenest.backend.module.vaccination.entity.VaccinationDose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VaccinationDoseRepository extends JpaRepository<VaccinationDose, Long> {
    List<VaccinationDose> findAllByVaccinationRecordIdOrderByDoseNumberAsc(Long vaccinationRecordId);
    Optional<VaccinationDose> findByVaccinationRecordIdAndDoseNumber(Long recordId, Integer doseNumber);

    @Query("SELECT v FROM VaccinationDose v " +
           "JOIN FETCH v.vaccinationRecord r " +
           "JOIN FETCH r.healthProfile hp " +
           "WHERE hp.family.id = :familyId " +
           "AND v.status = :status " +
           "ORDER BY v.scheduledDate ASC")
    List<VaccinationDose> findUpcomingDosesForFamily(
            @Param("familyId") Long familyId,
            @Param("status") com.carenest.backend.module.vaccination.enums.DoseStatus status);
}
