package com.carenest.backend.module.healthprofile.repository;

import com.carenest.backend.module.healthprofile.entity.HealthProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthProfileRepository extends JpaRepository<HealthProfile, Long> {
    List<HealthProfile> findByFamilyIdAndDeletedAtIsNull(Long familyId);
    List<HealthProfile> findByUserIdAndDeletedAtIsNull(Long userId);
}
