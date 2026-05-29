package com.carenest.backend.features.family.repository;

import com.carenest.backend.features.family.entity.FamilyInvitation;
import com.carenest.backend.features.family.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    List<FamilyInvitation> findReceivedInvitations(
            @Param("userId") Long userId,
            @Param("email") String email
    );
}
