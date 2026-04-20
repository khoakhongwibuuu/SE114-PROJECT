package com.carenest.backend.module.vaccination.entity;

import com.carenest.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "vaccination_doses", indexes = {
        @Index(name = "idx_vaccination_doses_scheduled", columnList = "scheduled_date")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaccinationDose extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaccination_record_id", nullable = false)
    private VaccinationRecord vaccinationRecord;

    @Column(name = "dose_number", nullable = false)
    private Integer doseNumber;

    @Column(name = "date_administered")
    private LocalDate dateAdministered;

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Column(length = 200)
    private String location;

    @Column(name = "administered_by", length = 200)
    private String administeredBy;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
