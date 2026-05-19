package com.carenest.backend.module.community.repository;

import com.carenest.backend.module.community.entity.GroupPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupPostRepository extends JpaRepository<GroupPost, Long> {
    List<GroupPost> findAllByCommunityGroupIdOrderByCreatedAtDesc(Long communityGroupId);
}
