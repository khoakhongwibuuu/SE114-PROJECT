package com.carenest.backend.module.cabinet.repository;

import com.carenest.backend.module.cabinet.entity.CabinetMedicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CabinetMedicineRepository extends JpaRepository<CabinetMedicine, Long> {
    Optional<CabinetMedicine> findByCabinetIdAndMedicineNameIgnoreCase(Long cabinetId, String medicineName);
}
