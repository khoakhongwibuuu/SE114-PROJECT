package com.carenest.backend.config.database;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.seed.qa-demo")
public class QaDemoSeedProperties {

    private boolean enabled = false;
    private String defaultPassword = "";
    private String adminEmail = "admin.demo@carenest.local";
    private String patientPrimaryEmail = "patient.one.demo@carenest.local";
    private String patientSecondaryEmail = "patient.two.demo@carenest.local";
    private String doctorPediatricEmail = "doctor.pediatrics.demo@carenest.local";
    private String doctorGeneralEmail = "doctor.general.demo@carenest.local";
    private String moderatorEmail = "moderator.demo@carenest.local";
    private String adminFullName = "Demo Admin";
    private String patientPrimaryFullName = "Demo Patient One";
    private String patientSecondaryFullName = "Demo Patient Two";
    private String doctorPediatricFullName = "Demo Pediatric Doctor";
    private String doctorGeneralFullName = "Demo General Doctor";
    private String moderatorFullName = "Demo Moderator";

    public String requireDefaultPassword() {
        if (defaultPassword == null || defaultPassword.isBlank()) {
            throw new IllegalStateException("Missing required seed property: app.seed.qa-demo.default-password");
        }
        return defaultPassword.trim();
    }
}
