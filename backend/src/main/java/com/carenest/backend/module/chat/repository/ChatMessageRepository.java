package com.carenest.backend.module.chat.repository;

import com.carenest.backend.module.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE m.family.id = :familyId ORDER BY m.createdAt DESC")
    Page<ChatMessage> findByFamilyIdOrderByCreatedAtDesc(@Param("familyId") Long familyId, Pageable pageable);
}
