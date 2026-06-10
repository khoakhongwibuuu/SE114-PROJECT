package com.carenest.backend.config.database;

import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.enums.Role;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.community.entity.UserGroupMembership;
import com.carenest.backend.features.community.enums.GroupRole;
import com.carenest.backend.features.community.repository.ChatGroupRepository;
import com.carenest.backend.features.community.repository.UserGroupMembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private static final String QA_MODERATOR_EMAIL = "qa.moderator@gmail.com";
    private static final String QA_MODERATOR_PASSWORD = "QaModerator123!";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChatGroupRepository chatGroupRepository;
    private final UserGroupMembershipRepository membershipRepository;
    private final com.carenest.backend.features.doctorverification.repository.DoctorVerificationRepository verificationRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedUser("admin@gmail.com", "Password123!", "Admin", Role.ADMIN);
        seedUser("doletuankiet06@gmail.com", "Kiet13012006", "Tuan Kiet", Role.USER);
        seedUser("kiet@gmail.com", "Kiet13012006", "Kiet Tuan", Role.USER);
        seedUser("bacsinhikhoa@gmail.com", "Bacsinhikhoa", "Bac si Nhi Khoa", Role.DOCTOR);
        seedUser("bacsidakhoa@gmail.com", "Bacsidakhoa", "Bac si Da Khoa", Role.DOCTOR);

        User qaModerator = seedUser(QA_MODERATOR_EMAIL, QA_MODERATOR_PASSWORD, "QA Moderator", Role.USER);
        ensureQaModeratorHostsAllGroups(qaModerator);
    }

    private User seedUser(String email, String password, String fullName, Role role) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setPasswordHash(passwordEncoder.encode(password));
            newUser.setFullName(fullName);
            newUser.setRole(role);
            newUser.setIsActive(true);
            newUser.setIsVerified(true);
            User saved = userRepository.save(newUser);
            log.info("Seeded account: {} ({})", email, role.name());
            return saved;
        });

        if (role == Role.DOCTOR && verificationRepository.findByUserId(user.getId()).isEmpty()) {
            com.carenest.backend.features.doctorverification.entity.DoctorVerification verification = new com.carenest.backend.features.doctorverification.entity.DoctorVerification();
            verification.setUser(user);
            verification.setSpecialty("Chuyên khoa " + fullName);
            verification.setHospitalName("Bệnh viện CareNest");
            verification.setCertificationNumber("123456789");
            verification.setDocumentUrl("https://example.com/document.jpg");
            verification.setStatus(com.carenest.backend.features.doctorverification.enums.VerificationStatus.APPROVED);
            verificationRepository.save(verification);
            log.info("Seeded verification for doctor: {}", email);
        }

        return user;
    }

    private void ensureQaModeratorHostsAllGroups(User qaModerator) {
        int assignments = 0;

        for (var group : chatGroupRepository.findAllByOrderByNameAsc()) {
            var existingMembership = membershipRepository.findByGroupIdAndUserId(group.getId(), qaModerator.getId());
            if (existingMembership.isPresent()) {
                UserGroupMembership membership = existingMembership.get();
                if (membership.getGroupRole() != GroupRole.HOST) {
                    membership.setGroupRole(GroupRole.HOST);
                    membershipRepository.save(membership);
                    assignments++;
                }
                continue;
            }

            membershipRepository.save(UserGroupMembership.builder()
                    .user(qaModerator)
                    .group(group)
                    .groupRole(GroupRole.HOST)
                    .build());
            assignments++;
        }

        log.info(
                "QA moderator ready: {} / {} -> HOST on {} groups",
                QA_MODERATOR_EMAIL,
                QA_MODERATOR_PASSWORD,
                assignments
        );
    }
}
