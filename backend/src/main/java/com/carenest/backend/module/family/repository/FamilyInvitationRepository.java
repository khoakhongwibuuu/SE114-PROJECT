package com.carenest.backend.module.family.repository;

import com.carenest.backend.module.family.entity.FamilyInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FamilyInvitationRepository extends JpaRepository<FamilyInvitation, Long> {
}
