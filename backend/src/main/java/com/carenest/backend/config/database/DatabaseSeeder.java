package com.carenest.backend.config.database;

import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.enums.Role;
import com.carenest.backend.features.auth.repository.UserRepository;
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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedUser("admin@gmail.com", "Password123!", "Admin", Role.ADMIN);
        seedUser("doletuankiet06@gmail.com", "Kiet13012006", "Tuan Kiet", Role.USER);
        seedUser("kiet@gmail.com", "Kiet13012006", "Kiet Tuan", Role.USER);
        seedUser("bacsinhikhoa@gmail.com", "Bacsinhikhoa", Role.DOCTOR, "Bác sĩ Nhi Khoa");
        seedUser("bacsidakhoa@gmail.com", "Bacsidakhoa", Role.DOCTOR, "Bác sĩ Đa Khoa");
    }

    private void seedUser(String email, String password, String fullName, Role role) {
        if (!userRepository.existsByEmail(email)) {
            User user = new User();
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setFullName(fullName);
            user.setRole(role);
            user.setIsActive(true);
            user.setIsVerified(true);
            userRepository.save(user);
            log.info("Đã tạo tài khoản mẫu: {} ({})", email, role.name());
        }
    }
    
    private void seedUser(String email, String password, Role role, String fullName) {
        seedUser(email, password, fullName, role);
    }
}
