package com.carenest.backend.module.family.dto.response;

import com.carenest.backend.module.family.enums.FamilyRole;
import com.carenest.backend.module.family.enums.InvitationStatus;
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
