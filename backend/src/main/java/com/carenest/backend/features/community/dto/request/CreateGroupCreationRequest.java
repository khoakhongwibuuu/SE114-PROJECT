package com.carenest.backend.features.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateGroupCreationRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String shortDescription;

    @NotBlank
    private String detailedPurpose;

    @NotBlank
    private String category;

    private String coverImageUrl;

    @NotBlank
    private String groupType;

    private String moderationIntent;

    private String communityRules;
}
