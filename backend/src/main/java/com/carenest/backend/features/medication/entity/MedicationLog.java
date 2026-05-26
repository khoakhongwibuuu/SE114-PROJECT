package com.carenest.backend.features.medication.entity;

import com.carenest.backend.core.entity.BaseEntity;
import com.carenest.backend.features.medication.enums.MedicationLogStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "medication_logs", indexes = {
        @Index(name = "idx_medication_logs_medication", columnList = "medication_id"),
        @Index(name = "idx_medication_logs_scheduled", columnList = "scheduled_time")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @Column(name = "scheduled_time", nullable = false)
    private Instant scheduledTime;

    @Column(name = "taken_time")
    private Instant takenTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MedicationLogStatus status = MedicationLogStatus.PENDING;

    @Column(name = "is_notified", nullable = false)
    @Builder.Default
    private Boolean isNotified = false;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
