package com.carenest.backend.features.community.repository;

import com.carenest.backend.features.community.entity.GroupPostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupPostCommentRepository extends JpaRepository<GroupPostComment, Long> {
    Page<GroupPostComment> findByGroupPostIdOrderByCreatedAtDesc(Long groupPostId, Pageable pageable);
    long countByGroupPostId(Long groupPostId);
}
