package com.carenest.backend.module.vaccination.entity;

import com.carenest.backend.common.entity.BaseEntity;
import com.carenest.backend.module.healthprofile.entity.HealthProfile;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vaccination_records", indexes = {
        @Index(name = "idx_vaccination_records_profile", columnList = "health_profile_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaccinationRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_profile_id", nullable = false)
    private HealthProfile healthProfile;

    @Column(name = "vaccine_name", nullable = false, length = 200)
    private String vaccineName;

    @Column(name = "total_doses", nullable = false)
    @Builder.Default
    private Integer totalDoses = 1;

    @Column(name = "dose_interval_days")
    private Integer doseIntervalDays;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
