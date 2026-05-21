package com.carenest.backend.module.community.repository;

import com.carenest.backend.module.community.entity.UserGroupMembership;
import com.carenest.backend.module.community.enums.GroupRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserGroupMembershipRepository extends JpaRepository<UserGroupMembership, Long> {

    Optional<UserGroupMembership> findByGroupIdAndUserId(Long groupId, Long userId);

    List<UserGroupMembership> findAllByUserIdOrderByJoinedAtDesc(Long userId);

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    boolean existsByGroupIdAndUserIdAndGroupRole(Long groupId, Long userId, GroupRole groupRole);

    long countByGroupId(Long groupId);

    @Modifying
    @Query("DELETE FROM UserGroupMembership membership WHERE membership.group.id = :groupId")
    void deleteAllByGroupId(@Param("groupId") Long groupId);
}
