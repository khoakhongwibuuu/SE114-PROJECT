package com.carenest.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA Auditing for @CreatedDate and @LastModifiedDate in BaseEntity.
 */
@Configuration
@EnableJpaAuditing
public class AuditConfig {
}
