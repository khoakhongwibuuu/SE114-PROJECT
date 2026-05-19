package com.carenest.backend.module.doctorverification.entity;

import com.carenest.backend.common.entity.BaseEntity;
import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.doctorverification.enums.VerificationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "doctor_verifications",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_doctor_verifications_user", columnNames = "user_id")
        },
        indexes = {
                @Index(name = "idx_doctor_verifications_status", columnList = "status"),
                @Index(name = "idx_doctor_verifications_certification", columnList = "certification_number")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorVerification extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "certification_number", nullable = false, length = 100)
    private String certificationNumber;

    @Column(nullable = false, length = 100)
    private String specialty;

    @Column(name = "hospital_name", nullable = false, length = 200)
    private String hospitalName;

    @Column(name = "document_url", nullable = false, length = 1000)
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VerificationStatus status = VerificationStatus.PENDING;

    @Lob
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
}
