package com.carenest.backend.features.community.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatGroupResponse {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String tags;
    private boolean isPrivate;
    private Long leadDoctorId;
    private String leadDoctorName;
    private long memberCount;
    private boolean joined;
    private String latestMessage;
    private java.time.Instant latestActivityAt;
}
