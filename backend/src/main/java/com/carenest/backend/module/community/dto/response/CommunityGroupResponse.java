package com.carenest.backend.module.community.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommunityGroupResponse {
    private Long id;
    private String name;
    private String description;
}
