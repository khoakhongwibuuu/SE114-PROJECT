package com.carenest.backend.features.admin.dto.response;

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
public class AdminUserAuditLogResponse {
    private Long id;
    private String action;
    private Long actorId;
    private String actorName;
    private Long targetUserId;
    private String targetUserName;
    private String targetUserEmail;
    private String previousRole;
    private String newRole;
    private String previousStatus;
    private String newStatus;
    private String reason;
    private Instant createdAt;
}
