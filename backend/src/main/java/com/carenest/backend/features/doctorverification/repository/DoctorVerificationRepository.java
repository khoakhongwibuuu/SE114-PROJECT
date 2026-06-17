package com.carenest.backend.features.doctorverification.repository;

import com.carenest.backend.features.doctorverification.entity.DoctorVerification;
import com.carenest.backend.features.doctorverification.enums.VerificationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorVerificationRepository extends JpaRepository<DoctorVerification, Long> {

    @EntityGraph(attributePaths = "user")
    Optional<DoctorVerification> findByUserId(Long userId);

    boolean existsByUserIdAndStatus(Long userId, VerificationStatus status);

    @EntityGraph(attributePaths = "user")
    List<DoctorVerification> findAllByStatusOrderByCreatedAtAsc(VerificationStatus status);

    @EntityGraph(attributePaths = "user")
    List<DoctorVerification> findAllByStatusOrderByUpdatedAtDesc(VerificationStatus status);

    long countByStatus(VerificationStatus status);
}
