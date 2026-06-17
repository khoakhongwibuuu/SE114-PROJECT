package com.carenest.backend.features.community.repository;

import com.carenest.backend.features.community.entity.GroupPost;
import com.carenest.backend.features.community.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface GroupPostRepository extends JpaRepository<GroupPost, Long> {
    Page<GroupPost> findAllByChatGroupIdOrderByCreatedAtDesc(Long chatGroupId, Pageable pageable);

    Page<GroupPost> findAllByChatGroupIdAndStatusOrderByCreatedAtDesc(Long chatGroupId, PostStatus status, Pageable pageable);

    Page<GroupPost> findAllByChatGroupIdAndAuthorIdOrderByCreatedAtDesc(Long chatGroupId, Long authorId, Pageable pageable);

    Optional<GroupPost> findFirstByChatGroupIdOrderByCreatedAtDesc(Long chatGroupId);

    @Modifying
    @Query("""
            UPDATE GroupPost post
            SET post.replyToPost = NULL
            WHERE post.chatGroup.id = :chatGroupId
            """)
    void clearRepliesByChatGroupId(@Param("chatGroupId") Long chatGroupId);

    @Modifying
    @Query("""
            UPDATE GroupPost post
            SET post.replyToPost = NULL
            WHERE post.replyToPost.id = :postId
            """)
    void clearRepliesByPostId(@Param("postId") Long postId);

    @Modifying
    @Query("DELETE FROM GroupPost post WHERE post.chatGroup.id = :chatGroupId")
    void deleteAllByChatGroupId(@Param("chatGroupId") Long chatGroupId);
}
