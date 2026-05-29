package com.carenest.backend.features.cabinet.entity;

import com.carenest.backend.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "cabinet_medicines", indexes = {
        @Index(name = "idx_cabinet_medicines_cabinet", columnList = "cabinet_id"),
        @Index(name = "idx_cabinet_medicines_expiry", columnList = "expiry_date")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CabinetMedicine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cabinet_id", nullable = false)
    private MedicineCabinet cabinet;

    @Column(name = "medicine_name", nullable = false, length = 200)
    private String medicineName;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(length = 50)
    private String unit;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "added_date", nullable = false)
    @Builder.Default
    private LocalDate addedDate = LocalDate.now();

    @Column(columnDefinition = "TEXT")
    private String notes;
}
