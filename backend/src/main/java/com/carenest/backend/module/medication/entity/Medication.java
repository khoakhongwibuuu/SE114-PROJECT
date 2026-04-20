package com.carenest.backend.module.medication.entity;

import com.carenest.backend.common.entity.BaseEntity;
import com.carenest.backend.module.healthprofile.entity.HealthProfile;
import com.carenest.backend.module.medication.enums.MedicationFrequency;
import com.carenest.backend.module.medication.enums.MedicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "medications", indexes = {
        @Index(name = "idx_medications_profile", columnList = "health_profile_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Medication extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_profile_id", nullable = false)
    private HealthProfile healthProfile;

    @Column(name = "medicine_name", nullable = false, length = 200)
    private String medicineName;

    @Column(length = 100)
    private String dosage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MedicationFrequency frequency;

    @Column(name = "times_per_day")
    @Builder.Default
    private Integer timesPerDay = 1;

    @Column(name = "time_slots", columnDefinition = "TEXT")
    private String timeSlots;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MedicationStatus status = MedicationStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
