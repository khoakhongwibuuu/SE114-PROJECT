package com.carenest.backend.module.family.repository;

import com.carenest.backend.module.family.entity.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {
    Optional<FamilyMember> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
