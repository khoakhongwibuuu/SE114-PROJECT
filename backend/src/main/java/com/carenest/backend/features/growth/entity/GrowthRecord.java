package com.carenest.backend.features.growth.entity;

import com.carenest.backend.core.entity.BaseEntity;
import com.carenest.backend.features.healthprofile.entity.HealthProfile;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "growth_records", indexes = {
        @Index(name = "idx_growth_records_profile", columnList = "health_profile_id"),
        @Index(name = "idx_growth_records_date", columnList = "record_date")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrowthRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_profile_id", nullable = false)
    private HealthProfile healthProfile;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "height_cm", precision = 5, scale = 1)
    private BigDecimal heightCm;

    @Column(name = "head_circumference_cm", precision = 4, scale = 1)
    private BigDecimal headCircumferenceCm;

    @Column(precision = 4, scale = 1)
    private BigDecimal bmi;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
