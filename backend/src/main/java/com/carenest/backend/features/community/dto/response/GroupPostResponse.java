package com.carenest.backend.features.community.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import com.carenest.backend.features.community.enums.PostStatus;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupPostResponse {
    private Long id;
    private Long chatGroupId;
    private String chatGroupName;
    private Long authorId;
    private String authorName;
    private String authorAvatarUrl;
    private String authorRole;
    private String title;
    private String content;
    private String tags;
    private Long replyToPostId;
    private String imageUrl;
    private Instant createdAt;
    private PostStatus status;
    private String rejectionReason;

    // Interaction stats
    private long likeCount;
    private long commentCount;
    private boolean likedByMe;
}
