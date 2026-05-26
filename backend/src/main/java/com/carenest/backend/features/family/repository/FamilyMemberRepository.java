package com.carenest.backend.features.family.repository;

import com.carenest.backend.features.family.entity.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {
    Optional<FamilyMember> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    List<FamilyMember> findAllByFamilyId(Long familyId);
    Optional<FamilyMember> findByFamilyIdAndUserId(Long familyId, Long userId);
    List<FamilyMember> findAllByUserId(Long userId);
    boolean existsByFamilyIdAndUserId(Long familyId, Long userId);

    /**
     * Used by FamilyContextInterceptor to verify BOLA/IDOR protection.
     * Joins to users table to look up by email (the principal name in SecurityContext).
     */
    @Query("SELECT fm FROM FamilyMember fm " +
           "JOIN fm.user u " +
           "WHERE fm.family.id = :familyId AND u.email = :email")
    Optional<FamilyMember> findByFamilyIdAndUserEmail(@Param("familyId") Long familyId,
                                                      @Param("email") String email);

    /**
     * Fetches all memberships for a user with their families eagerly loaded.
     * Used by getMyFamilies() to build FamilySummaryResponse list efficiently.
     */
    @Query("SELECT fm FROM FamilyMember fm " +
           "JOIN FETCH fm.family f " +
           "JOIN FETCH f.owner " +
           "WHERE fm.user.id = :userId " +
           "ORDER BY fm.joinedAt ASC")
    List<FamilyMember> findAllByUserIdWithFamily(@Param("userId") Long userId);
}
