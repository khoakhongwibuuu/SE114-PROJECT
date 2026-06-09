package com.carenest.backend.features.community.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupPostCommentResponse {
    private Long id;
    private Long groupPostId;
    private Long authorId;
    private String authorName;
    private String authorAvatarUrl;
    private String authorRole;
    private String content;
    private Instant createdAt;
}
