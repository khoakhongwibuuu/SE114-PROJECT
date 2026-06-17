package com.carenest.backend.features.community.repository;

import com.carenest.backend.features.community.entity.GroupGovernanceAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupGovernanceAuditLogRepository extends JpaRepository<GroupGovernanceAuditLog, Long> {
}
