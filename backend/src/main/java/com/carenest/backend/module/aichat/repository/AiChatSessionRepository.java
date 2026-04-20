package com.carenest.backend.module.aichat.repository;

import com.carenest.backend.module.aichat.entity.AiChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiChatSessionRepository extends JpaRepository<AiChatSession, Long> {
}
