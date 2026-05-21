package com.carenest.backend.module.community.repository;

import com.carenest.backend.module.community.entity.GroupPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupPostRepository extends JpaRepository<GroupPost, Long> {
    Page<GroupPost> findAllByCommunityGroupIdOrderByCreatedAtDesc(Long communityGroupId, Pageable pageable);
}
