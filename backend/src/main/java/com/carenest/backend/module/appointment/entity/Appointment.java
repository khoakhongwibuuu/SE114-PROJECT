package com.carenest.backend.module.appointment.entity;

import com.carenest.backend.common.entity.BaseEntity;
import com.carenest.backend.module.appointment.enums.AppointmentStatus;
import com.carenest.backend.module.healthprofile.entity.HealthProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_appointments_profile", columnList = "health_profile_id"),
        @Index(name = "idx_appointments_date", columnList = "appointment_date")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Appointment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_profile_id", nullable = false)
    private HealthProfile healthProfile;

    @Column(name = "doctor_name", length = 200)
    private String doctorName;

    @Column(name = "hospital_name", length = 200)
    private String hospitalName;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "appointment_date", nullable = false)
    private Instant appointmentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "result_notes", columnDefinition = "TEXT")
    private String resultNotes;

    @Column(name = "reminder_sent", nullable = false)
    @Builder.Default
    private Boolean reminderSent = false;
}
