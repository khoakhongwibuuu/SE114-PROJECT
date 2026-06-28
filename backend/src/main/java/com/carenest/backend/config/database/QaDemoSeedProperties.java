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
    private String adminEmail = "admin@carenest.com";
    private String patientPrimaryEmail = "anhkhoa.vv@gmail.com";
    private String patientSecondaryEmail = "doletuankiet06@gmail.com";
    private String doctorPediatricEmail = "bacsinhikhoa@gmail.com";
    private String doctorGeneralEmail = "bacsidakhoa@gmail.com";
    private String moderatorEmail = "qa.moderator@gmail.com";
    private String adminFullName = "CareNest Admin";
    private String patientPrimaryFullName = "Khoa Vu";
    private String patientSecondaryFullName = "Tuan Kiet";
    private String doctorPediatricFullName = "Bác sĩ Nhi Khoa";
    private String doctorGeneralFullName = "Bác sĩ Đa Khoa";
    private String moderatorFullName = "QA Moderator";

    public String requireDefaultPassword() {
        if (defaultPassword == null || defaultPassword.isBlank()) {
            throw new IllegalStateException("Missing required seed property: app.seed.qa-demo.default-password");
        }
        return defaultPassword.trim();
    }
}
