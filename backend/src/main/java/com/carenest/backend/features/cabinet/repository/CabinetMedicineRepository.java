package com.carenest.backend.features.cabinet.repository;

import com.carenest.backend.features.cabinet.entity.CabinetMedicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Repository
public interface CabinetMedicineRepository extends JpaRepository<CabinetMedicine, Long> {
    Optional<CabinetMedicine> findByCabinetIdAndMedicineNameIgnoreCase(Long cabinetId, String medicineName);

    List<CabinetMedicine> findByCabinetId(Long cabinetId);

    List<CabinetMedicine> findByCabinetIdAndExpiryDateBefore(Long cabinetId, LocalDate date);

    List<CabinetMedicine> findByCabinetIdAndQuantityLessThanEqual(Long cabinetId, Integer quantity);
}
