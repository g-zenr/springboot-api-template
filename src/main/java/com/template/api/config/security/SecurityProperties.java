package com.template.api.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
		boolean seedDemoUsers,
		boolean allowRegistration
) {
}
