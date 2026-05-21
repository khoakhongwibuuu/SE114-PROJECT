package com.carenest.backend.module.community.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArticleLikeResponse {
    private Long articleId;
    private boolean likedByMe;
    private long likeCount;
}
