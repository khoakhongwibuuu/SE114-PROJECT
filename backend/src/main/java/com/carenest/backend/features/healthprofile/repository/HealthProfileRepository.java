package com.carenest.backend.features.healthprofile.repository;

import com.carenest.backend.features.healthprofile.entity.HealthProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HealthProfileRepository extends JpaRepository<HealthProfile, Long> {
    List<HealthProfile> findByFamilyIdAndDeletedAtIsNull(Long familyId);
    List<HealthProfile> findByUserIdAndDeletedAtIsNull(Long userId);
    List<HealthProfile> findByFamilyId(Long familyId);
    Optional<HealthProfile> findByIdAndDeletedAtIsNull(Long id);
    Optional<HealthProfile> findFirstByFamilyIdAndUserIdAndDeletedAtIsNull(Long familyId, Long userId);
    boolean existsByUserIdAndFamilyIdAndDeletedAtIsNull(Long userId, Long familyId);
    boolean existsByUserIdAndFamilyIsNullAndDeletedAtIsNull(Long userId);
    Optional<HealthProfile> findFirstByUserIdAndFamilyIsNullAndDeletedAtIsNull(Long userId);
    List<HealthProfile> findByFamilyIdAndIsChildTrueAndDeletedAtIsNull(Long familyId);
}
