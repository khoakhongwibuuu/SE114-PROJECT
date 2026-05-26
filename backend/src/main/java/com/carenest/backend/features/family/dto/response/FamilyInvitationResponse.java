package com.carenest.backend.features.family.dto.response;

import com.carenest.backend.features.family.enums.FamilyRole;
import com.carenest.backend.features.family.enums.InvitationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FamilyInvitationResponse {
    private Long inviteId;
    private Long familyId;
    private String name;
    private String senderEmail;
    private String receiverEmail;
    private FamilyRole role;
    private InvitationStatus status;
    private Instant createdAt;
}
