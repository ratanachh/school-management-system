package com.visor.school.persistence

import org.springframework.data.jpa.repository.config.EnableJpaAuditing

/**
 * Configuration for JPA auditing
 * Note: AuditorAware implementation should be provided by the consuming service
 */
@EnableJpaAuditing
class AuditingConfig

