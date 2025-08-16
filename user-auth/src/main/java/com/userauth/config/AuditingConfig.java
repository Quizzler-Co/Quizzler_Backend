package com.userauth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// We’re creating a separate class because we’ll be adding more auditing related configurations later.
//  So it’s better to have a separate class.

@Configuration
@EnableJpaAuditing
public class AuditingConfig {
    // That's all here for now. We'll add more auditing configurations later.
}
