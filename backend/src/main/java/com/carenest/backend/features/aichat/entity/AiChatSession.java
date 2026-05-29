package com.carenest.backend.features.aichat.entity;

import com.carenest.backend.core.entity.BaseEntity;
import com.carenest.backend.features.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ai_chat_sessions", indexes = {
        @Index(name = "idx_ai_chat_sessions_user", columnList = "user_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 200)
    private String title;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";
}
