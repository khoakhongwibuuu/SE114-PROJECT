package com.carenest.backend.features.booking.repository;

import com.carenest.backend.features.booking.entity.ConsultationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultationMessageRepository extends JpaRepository<ConsultationMessage, Long> {
    List<ConsultationMessage> findAllByThreadIdOrderByCreatedAtAsc(Long threadId);
}
