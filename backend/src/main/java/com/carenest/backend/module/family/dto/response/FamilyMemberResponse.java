package com.carenest.backend.module.family.dto.response;

import com.carenest.backend.module.auth.dto.response.UserInfoResponse;
import com.carenest.backend.module.family.enums.FamilyRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FamilyMemberResponse {
    private Long id;
    private Long profileId;
    private UserInfoResponse user;
    private String fullName;
    private String avatarUrl;
    private FamilyRole role;
    private Instant joinedAt;
}
