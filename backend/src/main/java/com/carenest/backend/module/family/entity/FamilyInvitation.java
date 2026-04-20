package com.carenest.backend.module.family.entity;

import com.carenest.backend.common.entity.BaseEntity;
import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.family.enums.InvitationStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "family_invitations", indexes = {
        @Index(name = "idx_family_invitations_recipient", columnList = "recipient_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyInvitation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Column(name = "recipient_email", length = 255)
    private String recipientEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private InvitationStatus status = InvitationStatus.PENDING;
}
