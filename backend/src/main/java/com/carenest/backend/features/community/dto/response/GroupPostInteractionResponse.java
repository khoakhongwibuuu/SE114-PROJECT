package com.carenest.backend.features.community.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupPostInteractionResponse {
    private Long postId;
    private boolean likedByMe;
    private long likeCount;
}
