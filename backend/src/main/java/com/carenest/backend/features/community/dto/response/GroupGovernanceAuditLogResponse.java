package com.carenest.backend.features.community.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupGovernanceAuditLogResponse {
    private Long id;
    private String action;
    private String actorName;
    private Long actorId;
    private String targetUserName;
    private Long targetUserId;
    private String previousRole;
    private String newRole;
    private String note;
    private Instant createdAt;
}
