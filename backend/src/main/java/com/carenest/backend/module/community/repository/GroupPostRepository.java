package com.carenest.backend.module.community.repository;

import com.carenest.backend.module.community.entity.GroupPost;
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
    Page<GroupPost> findAllByCommunityGroupIdOrderByCreatedAtDesc(Long communityGroupId, Pageable pageable);

    Optional<GroupPost> findFirstByCommunityGroupIdOrderByCreatedAtDesc(Long communityGroupId);

    @Modifying
    @Query("""
            UPDATE GroupPost post
            SET post.replyToPost = NULL
            WHERE post.communityGroup.id = :communityGroupId
            """)
    void clearRepliesByCommunityGroupId(@Param("communityGroupId") Long communityGroupId);

    @Modifying
    @Query("DELETE FROM GroupPost post WHERE post.communityGroup.id = :communityGroupId")
    void deleteAllByCommunityGroupId(@Param("communityGroupId") Long communityGroupId);
}
