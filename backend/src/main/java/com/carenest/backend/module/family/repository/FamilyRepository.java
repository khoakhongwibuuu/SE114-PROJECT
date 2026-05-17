package com.carenest.backend.module.family.repository;

import com.carenest.backend.module.family.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FamilyRepository extends JpaRepository<Family, Long> {
    Optional<Family> findByJoinCode(String joinCode);
    boolean existsByJoinCode(String joinCode);
}
