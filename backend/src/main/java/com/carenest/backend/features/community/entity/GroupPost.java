package com.carenest.backend.features.community.entity;

import com.carenest.backend.core.entity.BaseEntity;
import com.carenest.backend.features.auth.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "group_posts", indexes = {
        @Index(name = "idx_group_posts_group", columnList = "community_group_id"),
        @Index(name = "idx_group_posts_author", columnList = "author_id"),
        @Index(name = "idx_group_posts_created_at", columnList = "created_at"),
        @Index(name = "idx_group_posts_reply", columnList = "reply_to_post_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupPost extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_group_id", nullable = false)
    private ChatGroup chatGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_post_id")
    private GroupPost replyToPost;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;
}
