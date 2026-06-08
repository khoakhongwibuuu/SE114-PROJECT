package com.carenest.backend.features.cabinet.entity;

import com.carenest.backend.core.entity.BaseEntity;
import com.carenest.backend.features.family.entity.Family;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String name = "Tủ thuốc gia đình";
}
