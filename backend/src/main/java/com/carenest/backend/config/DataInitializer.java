package com.carenest.backend.config;

import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.enums.Role;
import com.carenest.backend.features.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("admin@carenest.com").isEmpty()) {
            User admin = User.builder()
                    .email("admin@carenest.com")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .fullName("CareNest Admin")
                    .role(Role.ADMIN)
                    .isActive(true)
                    .isVerified(true)
                    .build();
            userRepository.save(admin);
            log.info("Successfully created default ADMIN account: admin@carenest.com / Admin@123");
        } else {
            log.info("Admin account already exists.");
        }
    }
}
