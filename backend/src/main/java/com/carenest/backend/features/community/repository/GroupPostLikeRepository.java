package com.carenest.backend.features.community.repository;

import com.carenest.backend.features.community.entity.GroupPostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupPostLikeRepository extends JpaRepository<GroupPostLike, Long> {
    long countByGroupPostId(Long groupPostId);
    boolean existsByGroupPostIdAndUserId(Long groupPostId, Long userId);
    Optional<GroupPostLike> findByGroupPostIdAndUserId(Long groupPostId, Long userId);
}
