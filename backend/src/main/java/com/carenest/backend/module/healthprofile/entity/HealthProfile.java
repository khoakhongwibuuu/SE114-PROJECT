package com.carenest.backend.module.healthprofile.entity;

import com.carenest.backend.common.entity.BaseEntity;
import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.auth.enums.Gender;
import com.carenest.backend.module.family.entity.Family;
import com.carenest.backend.module.healthprofile.enums.BloodType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "health_profiles", indexes = {
        @Index(name = "idx_health_profiles_user", columnList = "user_id"),
        @Index(name = "idx_health_profiles_family", columnList = "family_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthProfile extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Column(length = 50)
    private String relationship;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_type", length = 15)
    private BloodType bloodType;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Column(name = "chronic_diseases", columnDefinition = "TEXT")
    private String chronicDiseases;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "is_child", nullable = false)
    @Builder.Default
    private Boolean isChild = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
