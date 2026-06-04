package com.carenest.backend.features.community.repository;

import com.carenest.backend.features.community.entity.ReportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportTicketRepository extends JpaRepository<ReportTicket, Long> {
    void deleteAllByReportedPostId(Long reportedPostId);
}
