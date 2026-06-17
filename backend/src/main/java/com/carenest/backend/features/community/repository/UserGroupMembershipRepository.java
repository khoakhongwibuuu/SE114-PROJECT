package com.carenest.backend.features.community.repository;

import com.carenest.backend.features.community.entity.UserGroupMembership;
import com.carenest.backend.features.community.enums.GroupRole;
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

    List<UserGroupMembership> findAllByGroupIdOrderByGroupRoleDescJoinedAtAsc(Long groupId);

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    boolean existsByGroupIdAndUserIdAndGroupRole(Long groupId, Long userId, GroupRole groupRole);

    long countByGroupId(Long groupId);

    long countByGroupIdAndGroupRole(Long groupId, GroupRole groupRole);

    @Modifying
    @Query("DELETE FROM UserGroupMembership membership WHERE membership.group.id = :groupId")
    void deleteAllByGroupId(@Param("groupId") Long groupId);
}
