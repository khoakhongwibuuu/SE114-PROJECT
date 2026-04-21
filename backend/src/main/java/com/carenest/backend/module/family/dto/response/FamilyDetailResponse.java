package com.carenest.backend.module.family.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FamilyDetailResponse {
    private Long id;
    private String name;
    private Long ownerId;
    private Instant createdAt;
    
    private List<FamilyMemberResponse> members;
}
