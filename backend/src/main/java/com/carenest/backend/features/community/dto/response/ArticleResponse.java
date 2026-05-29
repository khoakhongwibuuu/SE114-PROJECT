package com.carenest.backend.features.community.dto.response;

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
    private String imageUrl;
    private Long authorId;
    private String authorName;
    private String authorAvatarUrl;
    private String authorRole;
    private String authorSpecialty;
    private String authorHospitalName;
    private Long authorPrivateGroupId;
    private Long authorSpecialtyGroupId;
    private Instant createdAt;
    private long likeCount;
    private long commentCount;
    private boolean likedByMe;
}
