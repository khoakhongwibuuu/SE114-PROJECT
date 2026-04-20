package com.carenest.backend.module.family.entity;

import com.carenest.backend.common.entity.BaseEntity;
import com.carenest.backend.module.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "families")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Family extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
}
