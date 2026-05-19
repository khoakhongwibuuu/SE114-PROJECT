package com.carenest.backend.module.community.repository;

import com.carenest.backend.module.community.entity.CommunityGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityGroupRepository extends JpaRepository<CommunityGroup, Long> {
    List<CommunityGroup> findAllByOrderByNameAsc();
}
