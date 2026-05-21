package com.carenest.backend.module.community.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ArticleCommentResponse {
    private Long id;
    private Long articleId;
    private Long authorId;
    private String authorName;
    private String content;
    private Instant createdAt;
}
