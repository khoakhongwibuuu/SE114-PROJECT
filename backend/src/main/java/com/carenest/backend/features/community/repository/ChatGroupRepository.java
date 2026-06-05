package com.carenest.backend.features.community.repository;

import com.carenest.backend.features.community.entity.ChatGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {
    List<ChatGroup> findAllByOrderByNameAsc();

    boolean existsByCategoryIgnoreCaseAndIsPrivateFalse(String category);

    Optional<ChatGroup> findFirstByCategoryIgnoreCaseAndIsPrivateFalse(String category);

    Optional<ChatGroup> findByLeadDoctorIdAndIsPrivateTrue(Long leadDoctorId);

    @Query("""
            SELECT g FROM ChatGroup g
            WHERE (:keyword IS NULL
                OR LOWER(CAST(g.name AS string)) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
                OR LOWER(CAST(g.description AS string)) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
                OR LOWER(CAST(g.tags AS string)) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')))
              AND (:category IS NULL OR LOWER(CAST(g.category AS string)) = LOWER(CAST(:category AS string)))
            ORDER BY g.name ASC
            """)
    List<ChatGroup> searchGroups(@Param("keyword") String keyword, @Param("category") String category);
}
