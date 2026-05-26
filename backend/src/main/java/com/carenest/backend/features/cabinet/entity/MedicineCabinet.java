package com.carenest.backend.features.cabinet.entity;

import com.carenest.backend.core.entity.BaseEntity;
import com.carenest.backend.features.family.entity.Family;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "medicine_cabinets", uniqueConstraints = {
        @UniqueConstraint(name = "uk_cabinet_family", columnNames = "family_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineCabinet extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @Column(nullable = false, length = 100)
    @Builder.Default
    private String name = "Tá»§ thuá»‘c gia Ä‘Ã¬nh";
}
