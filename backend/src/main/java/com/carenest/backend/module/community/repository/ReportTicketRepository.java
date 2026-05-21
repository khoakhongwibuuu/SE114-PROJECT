package com.carenest.backend.module.community.repository;

import com.carenest.backend.module.community.entity.ReportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportTicketRepository extends JpaRepository<ReportTicket, Long> {
}
