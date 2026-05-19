package com.carenest.backend.module.community.entity;

import com.carenest.backend.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "community_groups")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityGroup extends BaseEntity {

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;
}
