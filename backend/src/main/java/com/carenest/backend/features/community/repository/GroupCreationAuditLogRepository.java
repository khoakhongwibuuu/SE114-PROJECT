package com.carenest.backend.features.community.repository;

import com.carenest.backend.features.community.entity.GroupCreationAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupCreationAuditLogRepository extends JpaRepository<GroupCreationAuditLog, Long> {
}
