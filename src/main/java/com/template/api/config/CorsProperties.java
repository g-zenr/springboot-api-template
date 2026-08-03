package com.template.api.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
		String allowedOrigins,
		String allowedMethods,
		String allowedHeaders,
		boolean allowCredentials
) {

	public List<String> allowedOriginsList() {
		return split(allowedOrigins);
	}

	public List<String> allowedMethodsList() {
		return split(allowedMethods);
	}

	public List<String> allowedHeadersList() {
		return split(allowedHeaders);
	}

	private static List<String> split(String value) {
		if (value == null || value.isBlank()) {
			return List.of();
		}
		return Arrays.stream(value.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.toList();
	}
}
