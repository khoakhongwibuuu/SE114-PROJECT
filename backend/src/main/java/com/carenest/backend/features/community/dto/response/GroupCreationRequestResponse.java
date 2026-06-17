package com.carenest.backend.features.community.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class GroupCreationRequestResponse {
    private Long id;
    private Long requesterId;
    private String groupType;
    private String name;
    private String shortDescription;
    private String detailedPurpose;
    private String category;
    private String coverImageUrl;
    private String status;
    private String rejectionReason;
    private Long reviewerId;
    private Instant reviewedAt;
    private Instant createdAt;
}
