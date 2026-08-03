package com.template.api.common.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
		boolean enabled,
		int loginRequestsPerMinute,
		int apiRequestsPerMinute
) {
}
