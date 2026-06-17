package com.carenest.backend.features.admin.repository;

import com.carenest.backend.features.admin.entity.AdminUserAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminUserAuditLogRepository extends JpaRepository<AdminUserAuditLog, Long> {
    List<AdminUserAuditLog> findTop20ByOrderByCreatedAtDesc();
}
