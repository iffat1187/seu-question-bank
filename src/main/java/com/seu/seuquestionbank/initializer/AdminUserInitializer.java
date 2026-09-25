package com.seu.seuquestionbank.initializer;

import com.seu.seuquestionbank.enums.Role;
import com.seu.seuquestionbank.model.User;
import com.seu.seuquestionbank.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Idempotent seed for the first ADMIN account. Registration always creates STUDENT users,
 * so without an existing ADMIN this application would have no way to reach /admin/**.
 * Credentials can be overridden via ADMIN_EMAIL / ADMIN_PASSWORD / ADMIN_NAME environment
 * variables; otherwise documented defaults are used. No further ADMIN is created once one exists.
 */
@Component
public class AdminUserInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL:admin@seu.edu.bd}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:Admin@12345}")
    private String adminPassword;

    @Value("${ADMIN_NAME:System Administrator}")
    private String adminName;

    public AdminUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean hasAdmin = userRepository.findAll().stream()
                .anyMatch(u -> u.getRole() == Role.ADMIN);
        if (hasAdmin) {
            log.info("ADMIN user already exists, skipping admin creation");
            return;
        }
        String email = adminEmail == null ? "" : adminEmail.trim().toLowerCase();
        String password = adminPassword == null ? "" : adminPassword.trim();
        String name = adminName == null || adminName.isBlank() ? "System Administrator" : adminName.trim();
        if (email.isBlank() || password.length() < 8) {
            log.error("Cannot seed ADMIN: ADMIN_EMAIL or ADMIN_PASSWORD invalid. " +
                    "Provide ADMIN_EMAIL and ADMIN_PASSWORD (min 8 chars) OR remove them to use defaults.");
            return;
        }
        User admin = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.ADMIN)
                .createdAt(Instant.now())
                .build();
        userRepository.save(admin);
        log.info("ADMIN account created -> email=[{}] (override via ADMIN_EMAIL/ADMIN_PASSWORD env vars)", email);
    }
}