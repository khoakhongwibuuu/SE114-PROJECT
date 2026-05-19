package com.carenest.backend.module.community.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ArticleResponse {
    private Long id;
    private String title;
    private String content;
    private String tags;
    private Long authorId;
    private String authorName;
    private Instant createdAt;
}
