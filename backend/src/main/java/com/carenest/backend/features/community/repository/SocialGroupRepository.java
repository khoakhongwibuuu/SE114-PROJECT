package com.carenest.backend.features.community.repository;

import com.carenest.backend.features.community.entity.SocialGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocialGroupRepository extends JpaRepository<SocialGroup, Long> {
    Optional<SocialGroup> findFirstByCategoryIgnoreCase(String category);
}
