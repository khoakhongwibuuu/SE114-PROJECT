package com.carenest.backend.module.family.repository;

import com.carenest.backend.module.family.entity.FamilyInvitation;
import com.carenest.backend.module.family.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FamilyInvitationRepository extends JpaRepository<FamilyInvitation, Long> {
    List<FamilyInvitation> findAllBySender_IdOrderByCreatedAtDesc(Long senderId);

    boolean existsByFamily_IdAndRecipientEmailIgnoreCaseAndStatus(
            Long familyId,
            String recipientEmail,
            InvitationStatus status
    );

    @Query("""
            select invitation
            from FamilyInvitation invitation
            where invitation.recipient.id = :userId
               or lower(invitation.recipientEmail) = lower(:email)
            order by invitation.createdAt desc
            """)
    List<FamilyInvitation> findReceivedInvitations(Long userId, String email);
}
