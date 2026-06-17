package com.carenest.backend.features.chat.repository;

import com.carenest.backend.features.chat.entity.GroupChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupChatMessageRepository extends JpaRepository<GroupChatMessage, Long> {
    Page<GroupChatMessage> findByGroupId(Long groupId, Pageable pageable);
    List<GroupChatMessage> findAllByGroupId(Long groupId);
}
