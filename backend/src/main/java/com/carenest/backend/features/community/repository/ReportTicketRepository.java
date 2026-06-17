package com.carenest.backend.features.community.repository;

import com.carenest.backend.features.community.entity.ReportTicket;
import com.carenest.backend.features.community.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportTicketRepository extends JpaRepository<ReportTicket, Long> {
    Page<ReportTicket> findAllByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);
    List<ReportTicket> findAllByReportedPostId(Long reportedPostId);
    List<ReportTicket> findAllByReportedChatMessageId(Long reportedChatMessageId);
    long countByStatus(ReportStatus status);
    void deleteAllByReportedPostId(Long reportedPostId);
}
