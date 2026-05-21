package com.carenest.backend.module.community.repository;

import com.carenest.backend.module.community.entity.CommunityGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityGroupRepository extends JpaRepository<CommunityGroup, Long> {
    List<CommunityGroup> findAllByOrderByNameAsc();

    boolean existsByCategoryIgnoreCaseAndIsPrivateFalse(String category);

    Optional<CommunityGroup> findFirstByCategoryIgnoreCaseAndIsPrivateFalse(String category);

    Optional<CommunityGroup> findByLeadDoctorIdAndIsPrivateTrue(Long leadDoctorId);

    @Query("""
            SELECT g FROM CommunityGroup g
            WHERE (:keyword IS NULL
                OR LOWER(CAST(g.name AS string)) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
                OR LOWER(CAST(g.description AS string)) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
                OR LOWER(CAST(g.tags AS string)) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')))
              AND (:category IS NULL OR LOWER(CAST(g.category AS string)) = LOWER(CAST(:category AS string)))
            ORDER BY g.name ASC
            """)
    List<CommunityGroup> searchGroups(@Param("keyword") String keyword, @Param("category") String category);
}
