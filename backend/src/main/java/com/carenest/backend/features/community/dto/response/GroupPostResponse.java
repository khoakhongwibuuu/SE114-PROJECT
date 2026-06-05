package com.carenest.backend.features.community.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class GroupPostResponse {
    private Long id;
    private Long chatGroupId;
    private String chatGroupName;
    private Long authorId;
    private String authorName;
    private String authorRole;
    private String content;
    private Long replyToPostId;
    private String imageUrl;
    private Instant createdAt;
}
