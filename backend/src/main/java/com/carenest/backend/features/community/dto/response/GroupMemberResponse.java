package com.carenest.backend.features.community.dto.response;

import com.carenest.backend.features.community.enums.GroupRole;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class GroupMemberResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String avatarUrl;
    private GroupRole role;
    private boolean active;
    private Instant joinedAt;
}
