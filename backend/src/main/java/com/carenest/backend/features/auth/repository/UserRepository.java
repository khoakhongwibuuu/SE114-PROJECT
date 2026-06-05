package com.carenest.backend.features.auth.repository;

import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByVerificationToken(String token);
    Optional<User> findByResetPasswordToken(String token);
    List<User> findAllByRoleOrderByCreatedAtDesc(Role role);
    long countByRole(Role role);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE " +
            "(:search IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')))")
    org.springframework.data.domain.Page<User> findUsersBySearch(@org.springframework.data.repository.query.Param("search") String search, org.springframework.data.domain.Pageable pageable);
}
