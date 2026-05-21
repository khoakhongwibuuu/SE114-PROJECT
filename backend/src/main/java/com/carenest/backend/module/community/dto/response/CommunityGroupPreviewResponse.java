package com.carenest.backend.module.community.dto.response;

import com.carenest.backend.module.community.enums.GroupRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommunityGroupPreviewResponse {
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
    private GroupRole myRole;
    private String rules;
}
