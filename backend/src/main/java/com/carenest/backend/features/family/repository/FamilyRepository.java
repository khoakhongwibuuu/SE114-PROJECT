package com.carenest.backend.features.family.repository;

import com.carenest.backend.features.family.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FamilyRepository extends JpaRepository<Family, Long> {
    Optional<Family> findByJoinCode(String joinCode);
    boolean existsByJoinCode(String joinCode);
}
