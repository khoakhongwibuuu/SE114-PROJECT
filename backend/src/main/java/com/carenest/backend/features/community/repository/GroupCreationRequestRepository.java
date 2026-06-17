package com.carenest.backend.features.community.repository;

import com.carenest.backend.features.community.entity.GroupCreationRequest;
import com.carenest.backend.features.community.enums.GroupCreationRequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupCreationRequestRepository extends JpaRepository<GroupCreationRequest, Long> {

    boolean existsByRequesterIdAndStatus(Long requesterId, GroupCreationRequestStatus status);

    boolean existsByNameIgnoreCaseAndStatus(String name, GroupCreationRequestStatus status);

    @EntityGraph(attributePaths = {"requester", "reviewer"})
    List<GroupCreationRequest> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"requester", "reviewer"})
    List<GroupCreationRequest> findAllByRequesterIdOrderByCreatedAtDesc(Long requesterId);
}
