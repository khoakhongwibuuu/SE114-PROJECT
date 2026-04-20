package com.carenest.backend.module.cabinet.repository;

import com.carenest.backend.module.cabinet.entity.MedicineCabinet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicineCabinetRepository extends JpaRepository<MedicineCabinet, Long> {
    Optional<MedicineCabinet> findByFamilyId(Long familyId);
}
