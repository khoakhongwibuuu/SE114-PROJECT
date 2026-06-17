package com.carenest.backend.config;

import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.enums.Role;
import com.carenest.backend.features.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Override
    public void run(String... args) {
        boolean enabled = environment.getProperty("app.seed.default-admin.enabled", Boolean.class, false);
        if (!enabled) {
            log.info("Default dev ADMIN seed is disabled.");
            return;
        }

        String email = requiredSeedProperty("app.seed.default-admin.email");
        String password = requiredSeedProperty("app.seed.default-admin.password");
        String fullName = environment.getProperty("app.seed.default-admin.full-name", "CareNest Admin");

        if (userRepository.findByEmail(email).isEmpty()) {
            User admin = User.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode(password))
                    .fullName(fullName)
                    .role(Role.ADMIN)
                    .isActive(true)
                    .isVerified(true)
                    .build();
            userRepository.save(admin);
            log.info("Successfully created default dev ADMIN account: {}", email);
        } else {
            log.info("Default dev ADMIN account already exists.");
        }
    }

    private String requiredSeedProperty(String key) {
        String value = environment.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required dev seed property: " + key);
        }
        return value.trim();
    }
}
